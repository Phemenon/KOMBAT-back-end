package com.kombat.backend.map;

import com.kombat.backend.dto.GameDTO;
import com.kombat.backend.dto.MinionDTO;
import com.kombat.backend.dto.SpawnableHexDTO;
import com.kombat.backend.game.Engine.GameEngine;
import com.kombat.backend.game.Engine.GameState.Board_Direction.Board;
import com.kombat.backend.game.Engine.GameState.Board_Direction.Hex;
import com.kombat.backend.game.Engine.GameState.Mode_State.GameState;
import com.kombat.backend.game.Player_Minions.Minion;
import com.kombat.backend.game.Player_Minions.Player;
import com.kombat.backend.game.Player_Minions.TypeMinion;
import com.kombat.backend.model.RoomModel;

import java.util.ArrayList;
import java.util.List;

public class GameMapper {

    private GameMapper() {
    }

    public static GameDTO toDTO(GameEngine engine, RoomModel room) {
        GameState state = engine.getState();
        Board board = state.getBoard();

        List<MinionDTO> minions = new ArrayList<>();
        List<SpawnableHexDTO> spawnableHexes = new ArrayList<>();

        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getCols(); col++) {
                Hex hex = board.getHex(row, col);

                if (hex == null) {
                    continue;
                }

                Minion minion = board.getMinionAt(row, col);
                if (minion != null) {
                    minions.add(new MinionDTO(
                            row,
                            col,
                            minion.getOwner().getPlayerId(),
                            minion.getHp(),
                            resolveTypeName(minion)
                    ));
                }

                if (hex.isSpawnableP1() || hex.isSpawnableP2()) {
                    spawnableHexes.add(new SpawnableHexDTO(
                            row,
                            col,
                            hex.isSpawnableP1(),
                            hex.isSpawnableP2()
                    ));
                }
            }
        }

        List<String> events = engine.flushActionLog().stream()
                .map(Object::toString)
                .toList();

        Player currentPlayer = state.getCurrentPlayer();
        List<String> allowedTypes = currentPlayer.getAllowedTypes().stream()
                .map(TypeMinion::name)
                .toList();

        return new GameDTO(
                state.getCurrentPlayerIndex() + 1,
                state.getRound(),
                minions,

                Math.round(state.getRemainingBudget(0)),
                Math.round(state.getRemainingBudget(1)),

                state.getTotalHp(0),
                state.getTotalHp(1),

                state.getPlayer(0).getSpawnsLeft(),
                state.getPlayer(1).getSpawnsLeft(),

                engine.getResult().name(),
                events,
                allowedTypes,

                engine.getConfig().getHexPurchaseCost(),
                spawnableHexes,
                currentPlayer.hasBoughtHexThisTurn(),
                currentPlayer.hasSpawnedThisTurn(),
                room.getHostId(),
                state.getMode().name()
        );
    }

    private static String resolveTypeName(Minion minion) {
        try {
            return minion.getType().name();
        } catch (Exception ignored) {
            return "UNKNOWN";
        }
    }
}