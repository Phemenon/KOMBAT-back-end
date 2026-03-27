package com.kombat.backend.dto;

import lombok.Data;

@Data
public class BuyHexRequest {
    private String roomId;
    private String userId;
    private int row;
    private int col;
}