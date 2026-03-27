package com.kombat.backend.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameConfig {
    private long spawnCost;
    private long hexPurchaseCost;
    private long initBudget;
    private long turnBudget;
    private long maxBudget;
    private long baseInterestPct;
    private long initHp;
    private int maxSpawns;
    private int maxTurns;

    public GameConfig(
            long spawnCost,
            long hexPurchaseCost,
            long initBudget,
            long turnBudget,
            long maxBudget,
            long baseInterestPct,
            long initHp,
            int maxSpawns,
            int maxTurns
    ) {
        this.spawnCost = spawnCost;
        this.hexPurchaseCost = hexPurchaseCost;
        this.initBudget = initBudget;
        this.turnBudget = turnBudget;
        this.maxBudget = maxBudget;
        this.baseInterestPct = baseInterestPct;
        this.initHp = initHp;
        this.maxSpawns = maxSpawns;
        this.maxTurns = maxTurns;
    }

    public GameConfig copy() {
        return new GameConfig(
                spawnCost,
                hexPurchaseCost,
                initBudget,
                turnBudget,
                maxBudget,
                baseInterestPct,
                initHp,
                maxSpawns,
                maxTurns
        );
    }
}