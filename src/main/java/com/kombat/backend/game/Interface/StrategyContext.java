package com.kombat.backend.game.Interface;

import com.kombat.backend.game.Engine.GameState.Board_Direction.Direction;

public interface StrategyContext {
    long getVar(String id);
    void setVar(String id, long value);

    long opponent();
    long ally();
    long nearby(Direction dir);

    boolean move(Direction dir);
    void shoot(Direction dir, long cost);

    void done();
    boolean isDone();

    long random();
}
