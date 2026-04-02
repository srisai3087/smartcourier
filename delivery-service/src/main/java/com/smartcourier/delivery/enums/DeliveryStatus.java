package com.smartcourier.delivery.enums;

public enum DeliveryStatus {

    DRAFT,              // Delivery created but not yet confirmed by customer
    BOOKED,             // Customer confirmed - awaiting pickup
    PICKED_UP,          // Parcel collected by courier agent
    IN_TRANSIT,         // Parcel received at origin hub, moving to destination
    OUT_FOR_DELIVERY,   // Delivery agent assigned, en-route to receiver
    DELIVERED,          // Successfully delivered to receiver
    DELAYED,            // Exception: delayed due to weather/capacity etc.
    FAILED,             // Exception: delivery attempt failed
    RETURNED            // Exception: parcel returned to sender
}
