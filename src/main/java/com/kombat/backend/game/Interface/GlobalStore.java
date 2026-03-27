package com.kombat.backend.game.Interface;

public interface GlobalStore {
    long getGlobal(String id);
    void setGlobal(String id, long value);
}
