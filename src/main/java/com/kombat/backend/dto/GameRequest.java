package com.kombat.backend.dto;

import lombok.Data;

@Data
public class GameRequest {
    private String roomId;
    private String userId;
}