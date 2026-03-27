package com.kombat.backend.dto;

import lombok.Data;

@Data
public class SurrenderRequest {
    private String roomId;
    private String userId;
}