package com.kombat.backend.game.Engine.Info_Engine.GameEvent;

public enum Action {


    GAME_START,
    GAME_END,

    TURN_START,
    TURN_END,

    BUY_HEX_SUCCESS,
    BUY_HEX_FAILED,
    SPAWN_SUCCESS,
    SPAWN_FAILED_NO_MONEY,
    SPAWN_FAILED_HEX_OCCUPIED,
    SPAWN_FAILED_LIMIT_REACHED,
    INTEREST_GAINED,
    MOVE,
    STUCK,
    SHOOT_NO_BUDGET,
    SHOOT_HIT,
    SHOOT_KILL,
    SHOOT_MISS,
    DIE
}
