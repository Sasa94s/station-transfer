package com.petroapp.stationtransfer.models.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class EventDto {

    @NotBlank(message = "event_id is required")
    private String event_id;

    @NotBlank(message = "station_id is required")
    private String station_id;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "amount must be non-negative")
    private Double amount;

    @NotBlank(message = "status is required")
    private String status;

    @NotBlank(message = "created_at is required")
    private String created_at;
}