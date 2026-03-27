package com.kombat.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomConfigMessageRequest {
    private String userId;

    private Long spawnCost;
    private Long hexPurchaseCost;
    private Long initBudget;
    private Long turnBudget;
    private Long maxBudget;
    private Long baseInterestPct;
    private Long initHp;
    private Integer maxSpawns;
    private Integer maxTurns;
}