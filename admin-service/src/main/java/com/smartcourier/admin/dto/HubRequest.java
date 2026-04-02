package com.smartcourier.admin.dto;
import jakarta.validation.constraints.NotBlank; import lombok.Data;

@Data
public class HubRequest {
    @NotBlank private String hubName;
    @NotBlank private String city;
    @NotBlank private String state;
    @NotBlank private String pinCode;
}
