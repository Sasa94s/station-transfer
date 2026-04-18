package com.petroapp.stationtransfer.models.beans;

import com.petroapp.stationtransfer.models.dtos.EventDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class TransferRequest {

    @NotEmpty(message = "events must not be empty")
    @Valid
    private List<EventDto> events;
}