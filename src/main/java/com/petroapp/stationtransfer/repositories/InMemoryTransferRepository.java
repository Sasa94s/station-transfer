package com.petroapp.stationtransfer.repositories;

import com.petroapp.stationtransfer.models.entities.TransferEvent;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryTransferRepository implements TransferRepository {

    private final ConcurrentMap<String, TransferEvent> store = new ConcurrentHashMap<>();

    @Override
    public boolean saveIfAbsent(TransferEvent event) {
        return store.putIfAbsent(event.getEventId(), event) == null;
    }

    @Override
    public List<TransferEvent> findByStationId(String stationId) {
        return store.values().stream()
                .filter(e -> e.getStationId().equals(stationId))
                .toList();
    }
}