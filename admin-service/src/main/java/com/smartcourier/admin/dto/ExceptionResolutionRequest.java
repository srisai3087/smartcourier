package com.smartcourier.admin.dto;
import jakarta.validation.constraints.NotBlank; import lombok.Data;

@Data
public class ExceptionResolutionRequest {
    @NotBlank(message = "Resolution notes are required")
    private String resolution;
}
