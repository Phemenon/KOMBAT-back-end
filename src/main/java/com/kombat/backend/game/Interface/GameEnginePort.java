package com.kombat.backend.game.Interface;

import com.kombat.backend.game.Engine.GameState.Board_Direction.Direction;
import com.kombat.backend.game.Engine.GameState.Fight.CombatResult;
import com.kombat.backend.game.Player_Minions.Minion;
import com.kombat.backend.game.Player_Minions.Player;

public interface GameEnginePort {

    long getOwnerBudget(Player owner);
    long getOwnerInterestRate(Player owner);
    long getMaxBudget();
    long getSpawnsLeft(Player owner);

    long random();

    boolean move(Minion self, Direction dir);

    CombatResult shoot(Minion self, Direction dir, long cost);

    boolean isReadOnly(String id);

    long opponentInfo(Minion self);
    long allyInfo(Minion self);
    long nearbyInfo(Minion self, Direction dir);
}
