package com.petroapp.stationtransfer.models.beans;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StationSummaryResponse {
    private String station_id;
    private double total_approved_amount;
    private long events_count;
}