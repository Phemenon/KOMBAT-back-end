package com.kombat.backend.game.Engine.GameState.Fight;

public enum CombatOutcome {
    NO_BUDGET,     // ยิงไม่ได้เพราะเงินไม่พอ
    MISS,          // ยิงแล้วแต่ไม่โดนอะไร
    HIT,           // ยิงโดน แต่ยังไม่ตาย
    KILL           // ยิงแล้วตาย
}
