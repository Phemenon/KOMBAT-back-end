package com.kombat.backend.game.Engine.Info_Engine;

import com.kombat.backend.game.Player_Minions.Player;
import com.kombat.backend.game.Player_Minions.TypeMinion;

public record SpawnRequest(Player player, TypeMinion type, int row, int col) {
    public SpawnRequest {
        if (player == null) throw new IllegalArgumentException("Player must not be null");
        if (type == null) throw new IllegalArgumentException("Type must not be null");
    }
}
