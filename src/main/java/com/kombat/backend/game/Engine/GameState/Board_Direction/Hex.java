package com.kombat.backend.game.Engine.GameState.Board_Direction;

import com.kombat.backend.game.Player_Minions.Minion;
import com.kombat.backend.game.Player_Minions.Player;
import lombok.Getter;

public class Hex {
    @Getter
    private Minion minion;

    @Getter
    private boolean spawnableP1;

    @Getter
    private boolean spawnableP2;

    public Hex(boolean spawnableP1, boolean spawnableP2) {
        this.spawnableP1 = spawnableP1;
        this.spawnableP2 = spawnableP2;
    }

    public boolean isMinionHere() {
        return minion != null;
    }

    public void placeMinion(Minion minion) {
        this.minion = minion;
    }

    public void removeMinion() {
        this.minion = null;
    }

    public boolean canSpawn(Player player) {
        return switch (player.getPlayerId()) {
            case 1 -> spawnableP1;
            case 2 -> spawnableP2;
            default -> false;
        };
    }

    public void MakeEnable(Player p) {
        switch (p.getPlayerId()) {
            case 1 -> spawnableP1 = true;
            case 2 -> spawnableP2 = true;
        }
    }
}