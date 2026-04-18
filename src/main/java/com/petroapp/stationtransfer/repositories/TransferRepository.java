package com.petroapp.stationtransfer.repositories;

import com.petroapp.stationtransfer.models.entities.TransferEvent;

import java.util.List;

public interface TransferRepository {
    boolean saveIfAbsent(TransferEvent event);
    List<TransferEvent> findByStationId(String stationId);
}