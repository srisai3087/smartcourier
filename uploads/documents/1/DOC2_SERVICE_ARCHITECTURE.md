# SmartCourier – Service Architecture: Delivery, Tracking & Admin

This document explains how Delivery Service, Tracking Service, and Admin Service work,  
how they relate to each other, and how to explain the flow confidently.

---

## Architecture Overview

```
Client
  └── API Gateway (8080)  ← JWT validation + routing + load balancing
         ├── auth-service     (8081) ← registration, login, JWT issuance
         ├── delivery-service (8082) ← parcel lifecycle, state machine
         ├── tracking-service (8083) ← event history, documents, proofs
         └── admin-service    (8084) ← dashboard, hubs, reports, exceptions
```

Each service has its **own isolated MySQL database**. They never share tables.  
Cross-service data is fetched via **OpenFeign REST calls**, not DB joins.

---

## 1. Delivery Service (Port 8082)

### What it does
Delivery Service is the **core** of the system. It manages the full parcel lifecycle —  
from creation to delivery. It owns the **current state** of every delivery.

### Database: `delivery_db`
- `deliveries` table → one row per parcel, holds current status
- `addresses` table → sender and receiver address records
- `packages` table → physical parcel details (weight, dimensions)

### Key Entities
- **Delivery** → main entity, linked to Address (ManyToOne) and Package (OneToOne)
- **Address** → reusable address records (multiple deliveries can share an address)
- **Package** → parcel weight/dimensions, used for charge calculation

### State Machine
Every delivery follows a strict lifecycle. Invalid transitions throw HTTP 422.

```
DRAFT → BOOKED → PICKED_UP → IN_TRANSIT → OUT_FOR_DELIVERY → DELIVERED
                     │              │
                     ├── DELAYED ───┘
                     ├── FAILED → RETURNED
                     └── RETURNED
```

- **Customer** can only do: `DRAFT → BOOKED`
- **Admin** drives all other transitions via `PATCH /deliveries/{id}/status`

### Charge Calculation
Uses volumetric weight vs actual weight — whichever is higher gets billed.

```
volumetricWeight = (L × W × H) / 5000
billableWeight   = max(actual, volumetric)
charge           = max(billableWeight × rate, minimumCharge)
```

Rates (from `application.yml`):
- DOMESTIC: ₹50/kg, min ₹100
- EXPRESS: ₹100/kg, min ₹200
- INTERNATIONAL: ₹250/kg, min ₹500

### Feign Integration (Delivery → Tracking)
Every time admin updates a delivery status, Delivery Service **automatically** calls  
Tracking Service via OpenFeign to record the event:

```java
// Inside updateStatus() in DeliveryServiceImpl
trackingServiceClient.addEvent(event);  // ← Feign call
```

The Feign client (`TrackingServiceClient`) uses Eureka to resolve `tracking-service`  
to the actual host and port. No hardcoded URLs.

### ModelMapper Usage
Entity → DTO mapping uses ModelMapper to reduce boilerplate:
```java
DeliveryResponse response = modelMapper.map(d, DeliveryResponse.class);
```
Custom fields (nested address, package) are set manually after mapping.

---

## 2. Tracking Service (Port 8083)

### What it does
Tracking Service is the **history keeper**. It never stores the current status —  
it only appends new events every time something happens to a parcel.

Think of it like a parcel's journal: every scan, hub arrival, or status change gets  
written as a new row. Nothing is ever updated or deleted.

### Database: `tracking_db`
- `tracking_events` → immutable log, one row per event (indexed on `tracking_number`)
- `delivery_proofs` → signature/photo evidence at time of delivery
- `documents` → uploaded files (invoice, label, customs declaration)

### Key Entities
- **TrackingEvent** → append-only, records what happened, where, when
- **DeliveryProof** → recipient name, signature path, photo path, agent ID
- **Document** → file metadata + storage path on server filesystem

### How Events Get Created
Two ways:
1. **Automatically via Feign** – When Delivery Service updates a status, it calls  
   `POST /tracking/events` internally. The admin doesn't need to do anything.
2. **Manually by Admin** – Admin can also call `POST /tracking/events` directly  
   from Postman/frontend to log a custom event like "ARRIVED_AT_HUB".

### Feign Integration (Tracking → Delivery)
Before saving any event, Tracking Service **verifies the delivery exists** by calling  
Delivery Service via Feign:

```java
deliveryServiceClient.getDeliveryById(req.getDeliveryId());  // ← verification call
```

If delivery not found → throws `ResourceNotFoundException` → HTTP 404.  
If Feign fails for other reason → logs warning and continues (resilient behavior).

### Document Upload
Files are saved to the server filesystem at `./uploads/documents/{deliveryId}/filename`.  
The path is stored in the `documents` table. Max file size: 10MB.

### ModelMapper Usage
Event entity → EventDTO mapping:
```java
return modelMapper.map(saved, TrackingResponse.EventDTO.class);
```

---

## 3. Admin Service (Port 8084)

### What it does
Admin Service is the **operations center**. It has no direct ownership of delivery data —  
instead it aggregates data from other services and manages admin-specific entities.

### Database: `admin_db`
- `hubs` → courier sorting/distribution hub locations
- `exception_logs` → recorded delivery exceptions (DELAYED, FAILED, RETURNED)
- `reports` → pre-computed analytics snapshots

### Key Entities
- **Hub** → hub name, city, state, pin. Can be soft-disabled (`isActive = false`).
- **ExceptionLog** → delivery exceptions. `resolvedBy = null` means unresolved.
- **Report** → DAILY/WEEKLY/MONTHLY snapshots with delivery counts.

### Dashboard (Feign Integration)
The dashboard calls Delivery Service via Feign to get the **real total delivery count**:

```java
Map<String, Object> page = deliveryServiceClient.getAllDeliveries(0, 1);
long totalDeliveries = Long.parseLong(page.get("totalElements").toString());
```

Without Feign, the dashboard would always show zero for delivery counts.

### Exception Resolution
Admin resolves delivery exceptions via `PUT /admin/deliveries/{id}/resolve`:
```java
exceptionLog.setResolvedBy(adminId);
exceptionLog.setResolvedAt(LocalDateTime.now());
exceptionLog.setResolution(req.getResolution());
```

### ModelMapper Usage
Report entity → DTO mapping:
```java
return modelMapper.map(saved, ReportDTO.class);
```
Hub request DTO → Hub entity mapping:
```java
Hub hub = modelMapper.map(req, Hub.class);
```

---

## How the Three Services Relate

```
┌─────────────────────────────────────────────────────────────┐
│                     Admin (PATCH /status)                    │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────────┐
│  Delivery Service (delivery_db)                              │
│  - Holds CURRENT STATE of each delivery (one row)            │
│  - Validates status transition via state machine             │
│  - On transition: calls Tracking Service via Feign ────────► │
└──────────────────────────────────────────────────────────────┘
                                                               │
                                                               ▼
                                          ┌─────────────────────────────────────┐
                                          │  Tracking Service (tracking_db)     │
                                          │  - Appends new event row (history)  │
                                          │  - Verifies delivery via Feign back │
                                          │    to Delivery Service              │
                                          └─────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│  Admin Service (admin_db)                                    │
│  - Manages hubs, exceptions, reports                         │
│  - Gets delivery count via Feign → Delivery Service          │
└──────────────────────────────────────────────────────────────┘
```

### Key Design Points to Explain

**Why are databases separate?**  
Each service has its own DB (Database-per-Service pattern). This means one service  
going down doesn't affect the others' data. You can also scale them independently.

**Why no foreign keys across services?**  
`delivery_id` in `tracking_events` is just a `Long` column — not a `@ManyToOne` FK.  
Cross-service joins via SQL would couple the services tightly. Instead we use Feign.

**Why does Delivery Service call Tracking and not the other way?**  
Delivery owns the state change. It's responsible for notifying the tracking layer.  
This is the "event originator" pattern — whoever changes state fires the event.

**What is Feign?**  
OpenFeign is a declarative HTTP client. You write an interface, annotate it, and Spring  
generates the actual HTTP call at runtime. Eureka resolves the service name to IP:port  
automatically. No hardcoded URLs anywhere.

**What is the Load Balancer doing?**  
Spring Cloud LoadBalancer (included in `spring-cloud-starter-loadbalancer`) intercepts  
`lb://service-name` calls in the Gateway. It asks Eureka for all registered instances  
of that service and picks one using **round-robin**. If you run two delivery-service  
instances, the gateway alternates between them automatically.

---

## Security Flow

```
Client sends: Authorization: Bearer eyJhbGci...

API Gateway:
  1. Intercepts the request
  2. Checks if path is public (/auth/signup, /auth/login) → skip
  3. Validates JWT signature using shared secret key
  4. Extracts userId and role from JWT claims
  5. Adds headers: X-User-Id: 1, X-User-Role: ROLE_CUSTOMER
  6. Forwards request to downstream service

Downstream Service (Delivery/Tracking/Admin):
  1. HeaderAuthFilter reads X-User-Id and X-User-Role
  2. Creates Spring Security Authentication object
  3. Puts it in SecurityContextHolder
  4. @PreAuthorize("hasAuthority('ROLE_ADMIN')") checks it
```

Internal Feign calls between services bypass this by using `FeignConfig` which  
automatically adds `X-User-Id: 0` and `X-User-Role: ROLE_ADMIN` headers.
