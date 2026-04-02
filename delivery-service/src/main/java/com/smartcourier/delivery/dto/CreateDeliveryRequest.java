package com.smartcourier.delivery.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CreateDeliveryRequest {

    // DOMESTIC | EXPRESS | INTERNATIONAL
    @NotBlank(message = "Service type is required")
    private String serviceType;

    // Customer preferred pickup time
    private LocalDateTime scheduledPickup;

    @NotNull(message = "Sender address is required")
    @Valid
    private AddressRequest senderAddress;

    @NotNull(message = "Receiver address is required")
    @Valid
    private AddressRequest receiverAddress;

    @NotNull(message = "Package details are required")
    @Valid
    private PackageRequest packageDetails;

    // -----------------------------------------------------------------------
    // Nested DTOs (declared as static inner classes for co-location)
    // -----------------------------------------------------------------------

    @Data
    public static class AddressRequest {
        @NotBlank(message = "Full name is required")
        private String fullName;

        @NotBlank(message = "Street is required")
        private String street;

        private String street2;

        @NotBlank(message = "City is required")
        private String city;

        @NotBlank(message = "State is required")
        private String state;

        @NotBlank(message = "PIN code is required")
        private String pinCode;

        @NotBlank(message = "Country is required")
        private String country;

        @NotBlank(message = "Phone is required")
        private String phone;
    }

    @Data
    public static class PackageRequest {
        @NotNull(message = "Weight is required")
        @Positive(message = "Weight must be positive")
        private Double weightKg;

        private Double lengthCm;
        private Double widthCm;
        private Double heightCm;
        private String description;
        private Boolean isFragile = false;
    }
}
