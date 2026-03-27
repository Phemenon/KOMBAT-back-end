package com.kombat.backend.dto;

import lombok.Data;

@Data
public class SpawnMinionRequest {
    private String roomId;
    private String userId;
    private int row;
    private int col;
    private String type;
}