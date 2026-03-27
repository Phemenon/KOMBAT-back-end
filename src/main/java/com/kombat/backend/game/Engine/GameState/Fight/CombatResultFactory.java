package com.kombat.backend.game.Engine.GameState.Fight;

import com.kombat.backend.game.Player_Minions.Minion;

public class CombatResultFactory {
    private CombatResultFactory() {}

    public static CombatResult noBudget(Minion attacker) {
        return new CombatResult(
                CombatOutcome.NO_BUDGET,
                attacker, null, 0, 0, -1, -1
        );
    }

    public static CombatResult miss(
            Minion attacker, long totalCost, int row, int col
    ) {
        return new CombatResult(
                CombatOutcome.MISS,
                attacker, null, 0, totalCost, row, col
        );
    }

    public static CombatResult hit(
            Minion attacker, Minion target, long damage,
            long totalCost, int row, int col
    ) {
        return new CombatResult(
                CombatOutcome.HIT,
                attacker, target, damage,
                totalCost, row, col
        );
    }

    public static CombatResult kill(
            Minion attacker, Minion target, long damage,
            long totalCost, int row, int col
    ) {
        return new CombatResult(
                CombatOutcome.KILL,
                attacker, target, damage,
                totalCost, row, col
        );
    }
}
