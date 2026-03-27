package com.kombat.backend.game.Interface;

public interface LocalStore {
        long getLocal(String id);
        void setLocal(String id, long value);
}
