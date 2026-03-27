package com.kombat.backend.dto;

import lombok.Data;

@Data
public class ConfirmMinionDefenseRequest {
    private String userId;
    private String minionType;
    private long defenseFactor;

}