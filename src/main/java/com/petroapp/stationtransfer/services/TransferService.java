package com.petroapp.stationtransfer.services;

import com.petroapp.stationtransfer.models.beans.StationSummaryResponse;
import com.petroapp.stationtransfer.models.beans.TransferResponse;
import com.petroapp.stationtransfer.models.dtos.EventDto;
import com.petroapp.stationtransfer.models.entities.TransferEvent;
import com.petroapp.stationtransfer.repositories.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRepository repository;

    public TransferResponse ingest(List<EventDto> events) {
        int inserted = 0;
        int duplicates = 0;

        for (EventDto dto : events) {

            Instant createdAt = parseDate(dto.getCreated_at());

            TransferEvent event = new TransferEvent(
                    dto.getEvent_id(),
                    dto.getStation_id(),
                    dto.getAmount(),
                    dto.getStatus(),
                    createdAt
            );

            try {
                boolean saved = repository.saveIfAbsent(event);
                if (saved) inserted++;
                else duplicates++;
            } catch (Exception ex) {
                // In case DB unique constraint triggers (concurrency-safe)
                duplicates++;
            }
        }

        return new TransferResponse(inserted, duplicates);
    }

    private Instant parseDate(String value) {
        try {
            return Instant.parse(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid created_at format. Must be ISO8601");
        }
    }

    public StationSummaryResponse getSummary(String stationId) {
        List<TransferEvent> events = repository.findByStationId(stationId);

        double totalApproved = events.stream()
                .filter(e -> "approved".equalsIgnoreCase(e.getStatus()))
                .mapToDouble(TransferEvent::getAmount)
                .sum();

        return new StationSummaryResponse(
                stationId,
                totalApproved,
                events.size()
        );
    }
}