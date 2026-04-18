package com.petroapp.stationtransfer.models.entities;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferEvent {

    @Column(name = "event_id", nullable = false, updatable = false, unique = true)
    private String eventId;

    private String stationId;
    private double amount;
    private String status;
    private Instant createdAt;
}