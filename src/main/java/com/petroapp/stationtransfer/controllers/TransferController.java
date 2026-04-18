package com.petroapp.stationtransfer.controllers;

import com.petroapp.stationtransfer.models.beans.StationSummaryResponse;
import com.petroapp.stationtransfer.models.beans.TransferRequest;
import com.petroapp.stationtransfer.models.beans.TransferResponse;
import com.petroapp.stationtransfer.services.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService service;

    @PostMapping("/transfers")
    public ResponseEntity<TransferResponse> ingest(
            @Valid @RequestBody TransferRequest request
    ) {
        return ResponseEntity.ok(service.ingest(request.getEvents()));
    }

    @GetMapping("/stations/{stationId}/summary")
    public ResponseEntity<StationSummaryResponse> summary(
            @PathVariable String stationId
    ) {
        return ResponseEntity.ok(service.getSummary(stationId));
    }
}