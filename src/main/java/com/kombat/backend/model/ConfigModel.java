package com.kombat.backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ConfigModel {
    private long spawnCost;
    private long hexPurchaseCost;
    private long initBudget;
    private long turnBudget;
    private long maxBudget;
    private long baseInterestPct;
    private long initHp;
    private int maxSpawns;
    private int maxTurns;
}
