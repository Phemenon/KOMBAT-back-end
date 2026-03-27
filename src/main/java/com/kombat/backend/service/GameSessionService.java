package com.kombat.backend.service;

import com.kombat.backend.game.Engine.GameEngine;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameSessionService {

    private final Map<String, GameEngine> engineByRoomId = new ConcurrentHashMap<>();

    public void putEngine(String roomId, GameEngine engine) {
        engineByRoomId.put(roomId, engine);
    }

    public GameEngine getEngine(String roomId) {
        return engineByRoomId.get(roomId);
    }

    public boolean hasEngine(String roomId) {
        return engineByRoomId.containsKey(roomId);
    }

    public void removeEngine(String roomId) {
        engineByRoomId.remove(roomId);
    }
}