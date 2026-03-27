package com.kombat.backend.game.Engine.GameState.Fight;

import com.kombat.backend.game.Player_Minions.Minion;

public record CombatResult(
        CombatOutcome outcome,
        Minion attacker,
        Minion target,
        long damage,
        long totalCost,
        int targetRow,
        int targetCol
) {}
