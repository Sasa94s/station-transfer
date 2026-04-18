package com.petroapp.stationtransfer.models.beans;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransferResponse {
    private int inserted;
    private int duplicates;
}