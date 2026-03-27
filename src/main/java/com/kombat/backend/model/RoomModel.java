package com.kombat.backend.model;

import com.kombat.backend.config.GameConfig;
import com.kombat.backend.game.Engine.GameState.Mode_State.GameMode;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class RoomModel {
    private String roomId;
    private String hostId;
    private GameConfig config;
    private Map<String, PlayerModel> players = new ConcurrentHashMap<>();
    private boolean gameStarted = false;
    private GameMode mode;

    public RoomModel(String roomId, String hostId, GameConfig config, GameMode mode) {
        this.roomId = roomId;
        this.hostId = hostId;
        this.config = config;
        this.mode = mode;
    }
}