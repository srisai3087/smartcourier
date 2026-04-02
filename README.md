# 🚚 SmartCourier — Delivery Management System

**Spring Boot Microservices | MySQL | JPA/Hibernate | Spring Cloud Gateway | JWT Security**

---

## 📌 Project Overview

SmartCourier is an enterprise-grade courier and parcel delivery management system built using the **Spring Boot Microservices** architecture. Customers can book deliveries, track parcels in real time, and upload documents. Admins can monitor the full delivery pipeline, resolve exceptions, manage hubs, and generate reports.

---

## 🏗️ Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    CLIENT (Angular / Postman)                │
└────────────────────────────┬────────────────────────────────┘
                             │ HTTP (all traffic)
                             ▼
┌─────────────────────────────────────────────────────────────┐
│              Spring Cloud API Gateway  :8080                │
│   • JWT Validation (JwtAuthFilter)                          │
│   • Route to downstream services                            │
│   • Adds X-User-Id, X-User-Role headers                     │
└──────┬────────────┬──────────────┬───────────────┬──────────┘
       │            │              │               │
       ▼            ▼              ▼               ▼
  Auth Svc    Delivery Svc   Tracking Svc    Admin Svc
  :8081        :8082           :8083           :8084
  auth_db      delivery_db    tracking_db     admin_db
```

---

## 🗂️ Project Structure

```
smartcourier/
├── pom.xml                         ← Parent POM (manages all module versions)
├── README.md
├── sql/
│   └── init.sql                    ← Database & seed data setup
│
├── api-gateway/                    ← Spring Cloud Gateway (Port 8080)
│   └── src/main/java/.../gateway/
│       ├── ApiGatewayApplication.java
│       ├── config/JwtUtil.java     ← JWT validation utility
│       └── filter/JwtAuthFilter.java ← Custom gateway filter
│
├── auth-service/                   ← Auth Microservice (Port 8081)
│   └── src/main/java/.../auth/
│       ├── entity/                 ← User, Role (JPA entities)
│       ├── repository/             ← UserRepository, RoleRepository
│       ├── service/                ← AuthService (interface)
│       ├── serviceimpl/            ← AuthServiceImpl (implementation)
│       ├── controller/             ← AuthController
│       ├── dto/                    ← SignupRequest, LoginRequest, AuthResponse
│       ├── exception/              ← GlobalExceptionHandler, custom exceptions
│       └── config/                 ← SecurityConfig (BCrypt, filter chain)
│
├── delivery-service/               ← Delivery Microservice (Port 8082)
│   └── src/main/java/.../delivery/
│       ├── entity/                 ← Delivery, Package, Address
│       ├── enums/                  ← DeliveryStatus (state machine)
│       ├── repository/             ← DeliveryRepository
│       ├── service/                ← DeliveryService (interface)
│       ├── serviceimpl/            ← DeliveryServiceImpl (charge calc + state machine)
│       ├── controller/             ← DeliveryController
│       ├── dto/                    ← CreateDeliveryRequest, DeliveryResponse
│       └── exception/              ← GlobalExceptionHandler
│
├── tracking-service/               ← Tracking Microservice (Port 8083)
│   └── src/main/java/.../tracking/
│       ├── entity/                 ← TrackingEvent, Document, DeliveryProof
│       ├── repository/             ← One per entity
│       ├── service/                ← TrackingService (interface)
│       ├── serviceimpl/            ← TrackingServiceImpl (file upload + events)
│       ├── controller/             ← TrackingController
│       └── dto/                    ← TrackingResponse, ProofResponse
│
└── admin-service/                  ← Admin Microservice (Port 8084)
    └── src/main/java/.../admin/
        ├── entity/                 ← ExceptionLog, Hub, Report
        ├── repository/             ← One per entity
        ├── service/                ← AdminService (interface)
        ├── serviceimpl/            ← AdminServiceImpl
        ├── controller/             ← AdminController
        └── dto/                    ← DashboardResponse, HubRequest, ReportDTO
```

---

## ⚙️ Spring Modules Covered

| Module | Where Used |
|--------|-----------|
| **Spring Boot Web** | All 4 microservices — REST controllers |
| **Spring Cloud Gateway** | `api-gateway` — routing, filter chain |
| **Spring Security 6** | Auth Service (BCrypt), all services (stateless config) |
| **Spring Data JPA** | All 4 microservices — repositories, entity management |
| **Hibernate ORM** | JPA provider — auto DDL, entity-to-table mapping |
| **Spring Validation** | All DTOs — `@Valid`, `@NotBlank`, `@Email`, `@Positive` |
| **Spring Boot Actuator** | Exposed via `/actuator/health` (add dependency optionally) |
| **SpringDoc OpenAPI 3** | Swagger UI on each service port |
| **JJWT (io.jsonwebtoken)** | JWT generation (Auth) + validation (Gateway) |
| **JUnit 5 + Mockito** | Unit tests in `auth-service` |
| **Lombok** | All services — `@Data`, `@Builder`, `@Slf4j`, `@RequiredArgsConstructor` |

---

## 🔄 Request Flow (Step by Step)

### Signup Flow
```
1. POST /gateway/auth/signup  (no JWT needed)
2. Gateway → routes to Auth Service :8081/auth/signup
3. AuthServiceImpl: check email unique → hash password (BCrypt) → save User
4. JwtService: generate token with {userId, email, role} claims
5. Return: { accessToken, refreshToken, role, userId }
```

### Create Delivery Flow
```
1. POST /gateway/deliveries  (JWT required)
2. Gateway JwtAuthFilter: validate token → extract userId, role
3. Gateway adds headers: X-User-Id: 5, X-User-Role: ROLE_CUSTOMER
4. Routes to Delivery Service :8082/deliveries
5. DeliveryController reads X-User-Id header → passes to service
6. DeliveryServiceImpl:
   a. Map DTO → Address, Package, Delivery entities
   b. Calculate charge (actual vs volumetric weight × service rate)
   c. Generate tracking number (SC-XXXXXXXXXX)
   d. Save to delivery_db (status = DRAFT)
7. Return DeliveryResponse with trackingNumber and chargeAmount
```

### Track Parcel Flow
```
1. GET /gateway/tracking/{trackingNumber}  (JWT required)
2. Gateway validates JWT, forwards to Tracking Service :8083
3. TrackingServiceImpl: query TrackingEvent by trackingNumber (uses DB index)
4. Return full event timeline ordered by eventTime DESC
```

### Admin Resolve Exception Flow
```
1. PUT /gateway/admin/deliveries/{id}/resolve  (ADMIN JWT required)
2. Gateway validates JWT, checks role = ROLE_ADMIN
3. Forwards to Admin Service :8084 with X-User-Id header
4. AdminServiceImpl: find ExceptionLog → set resolvedBy, resolvedAt, resolution
5. Save and return updated ExceptionLog
```

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.9+
- MySQL 8.0
- Postman (for API testing)

### Step 1: Database Setup
```sql
-- Run this first in MySQL Workbench or CLI:
SOURCE sql/init.sql;
```

### Step 2: Configure Passwords
Update `password: root` in each service's `application.yml` with your MySQL password:
- `auth-service/src/main/resources/application.yml`
- `delivery-service/src/main/resources/application.yml`
- `tracking-service/src/main/resources/application.yml`
- `admin-service/src/main/resources/application.yml`

### Step 3: Build All Services
```bash
# From the root smartcourier/ directory:
mvn clean install -DskipTests
```

### Step 4: Start Services (in this order)
```bash
# Terminal 1 — Auth Service
cd auth-service && mvn spring-boot:run

# Terminal 2 — Delivery Service
cd delivery-service && mvn spring-boot:run

# Terminal 3 — Tracking Service
cd tracking-service && mvn spring-boot:run

# Terminal 4 — Admin Service
cd admin-service && mvn spring-boot:run

# Terminal 5 — API Gateway (start LAST)
cd api-gateway && mvn spring-boot:run
```

---

## 🔌 API Reference & Testing Guide

### Base URL
All requests go through the **API Gateway**: `http://localhost:8080`

### 1. Authentication APIs (No Token Required)

#### Register New User
```
POST http://localhost:8080/gateway/auth/signup
Content-Type: application/json

{
  "fullName": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "phone": "9876543210"
}
```
**Response:** `{ "accessToken": "eyJ...", "role": "ROLE_CUSTOMER", "userId": 1 }`

#### Login
```
POST http://localhost:8080/gateway/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```
**→ Copy the `accessToken` from response. Use it as Bearer token for all subsequent requests.**

---

### 2. Delivery APIs (Token Required)

#### Create Delivery
```
POST http://localhost:8080/gateway/deliveries
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "serviceType": "DOMESTIC",
  "scheduledPickup": "2025-07-01T10:00:00",
  "senderAddress": {
    "fullName": "John Doe",
    "street": "123 MG Road",
    "city": "Mumbai",
    "state": "Maharashtra",
    "pinCode": "400001",
    "country": "India",
    "phone": "9876543210"
  },
  "receiverAddress": {
    "fullName": "Jane Smith",
    "street": "45 Park Street",
    "city": "Delhi",
    "state": "Delhi",
    "pinCode": "110001",
    "country": "India",
    "phone": "9123456789"
  },
  "packageDetails": {
    "weightKg": 2.5,
    "lengthCm": 30,
    "widthCm": 20,
    "heightCm": 15,
    "description": "Electronics",
    "isFragile": true
  }
}
```

#### Get My Deliveries
```
GET http://localhost:8080/gateway/deliveries/my
Authorization: Bearer {accessToken}
```

#### Track by Tracking Number
```
GET http://localhost:8080/gateway/tracking/SC-XXXXXXXXXX
Authorization: Bearer {accessToken}
```

---

### 3. Admin APIs (Admin Token Required)

First, create an admin user by manually updating a user's role in MySQL:
```sql
INSERT INTO auth_db.roles (name) VALUES ('ROLE_ADMIN');
-- After signup, update user's role in user_roles table to ROLE_ADMIN
```

#### Admin Dashboard
```
GET http://localhost:8080/gateway/admin/dashboard
Authorization: Bearer {adminToken}
```

#### Update Delivery Status
```
PATCH http://localhost:8082/deliveries/{id}/status?status=PICKED_UP
Authorization: Bearer {adminToken}
```

#### Create Hub
```
POST http://localhost:8080/gateway/admin/hubs
Authorization: Bearer {adminToken}
Content-Type: application/json

{
  "hubName": "Mumbai Central Hub",
  "city": "Mumbai",
  "state": "Maharashtra",
  "pinCode": "400001"
}
```

---

## 📚 Swagger UI (Per Service)

| Service | Swagger URL |
|---------|------------|
| Auth Service | http://localhost:8081/swagger-ui.html |
| Delivery Service | http://localhost:8082/swagger-ui.html |
| Tracking Service | http://localhost:8083/swagger-ui.html |
| Admin Service | http://localhost:8084/swagger-ui.html |

> **Tip:** On each Swagger page, click **Authorize**, enter `Bearer {your_token}` to test protected endpoints directly.

---

## 🔐 Security Architecture

```
JWT Token Claims:
{
  "sub": "john@example.com",     ← Spring Security username
  "userId": 1,                   ← Forwarded as X-User-Id header
  "role": "ROLE_CUSTOMER",       ← Forwarded as X-User-Role header
  "iat": 1720000000,
  "exp": 1720003600              ← 1 hour validity
}

Gateway Filter Chain:
  1. Check Authorization: Bearer <token> header
  2. Validate JWT signature (HMAC-SHA256 with shared secret)
  3. Check expiry (exp claim)
  4. Extract userId + role → add as downstream headers
  5. Forward to microservice (token stripped)
```

---

## 🗃️ Database Design

| Service | Database | Tables |
|---------|----------|--------|
| Auth Service | `auth_db` | users, roles, user_roles |
| Delivery Service | `delivery_db` | deliveries, packages, addresses |
| Tracking Service | `tracking_db` | tracking_events, documents, delivery_proofs |
| Admin Service | `admin_db` | exception_logs, hubs, reports |

**Database-per-Service pattern** — no cross-database joins. Services share data via REST API calls only.

---

## 📦 Delivery Charge Calculation

```
volumetricWeight = (lengthCm × widthCm × heightCm) / 5000
billableWeight   = max(actualWeight, volumetricWeight)

Service Rates:
  DOMESTIC:      ₹50/kg  (min ₹100)
  EXPRESS:       ₹100/kg (min ₹200)
  INTERNATIONAL: ₹250/kg (min ₹500)

charge = max(billableWeight × rate, minimumCharge)
```

---

## 🧪 Running Tests

```bash
# Run all unit tests
mvn test

# Run tests for specific service
cd auth-service && mvn test
```

---

## 📞 Port Reference

| Service | Port |
|---------|------|
| API Gateway | 8080 |
| Auth Service | 8081 |
| Delivery Service | 8082 |
| Tracking Service | 8083 |
| Admin Service | 8084 |

