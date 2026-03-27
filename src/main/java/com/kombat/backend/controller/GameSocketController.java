package com.kombat.backend.controller;

import com.kombat.backend.dto.GameRequest;
import com.kombat.backend.dto.SurrenderRequest;
import com.kombat.backend.game.Engine.GameEngine;
import com.kombat.backend.game.Engine.Info_Engine.GameResult;
import com.kombat.backend.model.RoomModel;
import com.kombat.backend.service.GameSessionService;
import com.kombat.backend.service.RoomService;
import com.kombat.backend.map.GameMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.kombat.backend.dto.SpawnMinionRequest;
import com.kombat.backend.game.Engine.GameState.Mode_State.GameState;
import com.kombat.backend.game.Player_Minions.Player;
import com.kombat.backend.game.Player_Minions.TypeMinion;
import com.kombat.backend.dto.BuyHexRequest;

@Controller
@RequiredArgsConstructor
public class GameSocketController {

    private final GameSessionService gameSessionService;
    private final RoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;

    private boolean isBotTurn(GameEngine engine) {
        GameState state = engine.getState();
        int index = state.getCurrentPlayerIndex();

        return (index == 0 && state.isPlayer1Bot())
                || (index == 1 && state.isPlayer2Bot());
    }

    @MessageMapping("/game/sync")
    public void sync(GameRequest req) {
        GameEngine engine = gameSessionService.getEngine(req.getRoomId());

        if (engine == null) {
            throw new RuntimeException("Game not found");
        }

        RoomModel room = roomService.getRoom(req.getRoomId());

        if (!room.getPlayers().containsKey(req.getUserId())) {
            throw new RuntimeException("User is not in this room");
        }

        messagingTemplate.convertAndSend(
                "/topic/game/" + req.getRoomId(),
                GameMapper.toDTO(engine, room)
        );
    }

    @MessageMapping("/game/end-turn")
    public void endTurn(GameRequest req) {
        GameEngine engine = gameSessionService.getEngine(req.getRoomId());

        if (engine == null) {
            throw new RuntimeException("Game not found");
        }

        RoomModel room = roomService.getRoom(req.getRoomId());
        GameState state = engine.getState();

        // AUTO: ให้ host เป็นคนกด advance flow ได้ แม้ไม่ใช่ owner ของ current turn
        if (state.getMode() == com.kombat.backend.game.Engine.GameState.Mode_State.GameMode.AUTO) {
            if (!room.getHostId().equals(req.getUserId())) {
                throw new RuntimeException("Only host can advance AUTO mode");
            }

            // กด 1 ครั้ง = เดิน 1 เทิร์น
            engine.runTurn();
        } else {
            // DUEL / SOLITAIRE ใช้กติกา ownership ตามเดิม
            validateUserOwnsCurrentTurn(room, engine, req.getUserId());

            // เทิร์นของผู้เล่นปัจจุบัน
            engine.runTurn();

            // SOLITAIRE: ถ้าถัดไปเป็น bot ให้ bot เล่นต่อจนกว่าจะกลับมาคน
            if (state.getMode() == com.kombat.backend.game.Engine.GameState.Mode_State.GameMode.SOLITAIRE) {
                int safety = 0;

                while (isBotTurn(engine)
                        && engine.getResult() == GameResult.ONGOING
                        && safety < 10) {
                    engine.runTurn();
                    safety++;
                }
            }
        }

        messagingTemplate.convertAndSend(
                "/topic/game/" + req.getRoomId(),
                GameMapper.toDTO(engine, room)
        );
    }

    @MessageMapping("/game/spawn")
    public void spawn(SpawnMinionRequest req) {
        GameEngine engine = gameSessionService.getEngine(req.getRoomId());

        if (engine == null) {
            throw new RuntimeException("Game not found");
        }

        RoomModel room = roomService.getRoom(req.getRoomId());
        validateUserOwnsCurrentTurn(room, engine, req.getUserId());

        GameState state = engine.getState();
        Player currentPlayer = state.getCurrentPlayer();

        TypeMinion selectedType = currentPlayer.getAllowedTypes().stream()
                .filter(type -> type.name().equals(req.getType()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unknown or unavailable minion type: " + req.getType()));

        boolean success = engine.spawnMinion(
                currentPlayer,
                selectedType,
                req.getRow(),
                req.getCol()
        );

        if (!success) {
            throw new RuntimeException("Cannot spawn minion at this hex");
        }

        messagingTemplate.convertAndSend(
                "/topic/game/" + req.getRoomId(),
                GameMapper.toDTO(engine, room)
        );
    }

    @MessageMapping("/game/buy-hex")
    public void buyHex(BuyHexRequest req) {
        GameEngine engine = gameSessionService.getEngine(req.getRoomId());

        if (engine == null) {
            throw new RuntimeException("Game not found");
        }

        RoomModel room = roomService.getRoom(req.getRoomId());
        validateUserOwnsCurrentTurn(room, engine, req.getUserId());

        GameState state = engine.getState();
        Player currentPlayer = state.getCurrentPlayer();

        boolean success = engine.buyHex(currentPlayer, req.getRow(), req.getCol());

        if (!success) {
            throw new RuntimeException("Cannot buy this hex");
        }

        messagingTemplate.convertAndSend(
                "/topic/game/" + req.getRoomId(),
                GameMapper.toDTO(engine, room)
        );
    }

    @MessageMapping("/game/surrender")
    public void surrender(SurrenderRequest req) {
        GameEngine engine = gameSessionService.getEngine(req.getRoomId());

        if (engine == null) {
            throw new RuntimeException("Game not found");
        }

        RoomModel room = roomService.getRoom(req.getRoomId());

        System.out.println("SURRENDER => roomId=" + req.getRoomId()
                + ", userId=" + req.getUserId()
                + ", hostId=" + room.getHostId());

        if (req.getUserId() == null || req.getUserId().isBlank()) {
            throw new RuntimeException("userId is required");
        }

        if (!room.getPlayers().containsKey(req.getUserId())) {
            throw new RuntimeException("Player not found in room");
        }

        boolean isHost = room.getHostId().equals(req.getUserId());

        if (isHost) {
            engine.setResult(GameResult.PLAYER2_WIN);
        } else {
            engine.setResult(GameResult.PLAYER1_WIN);
        }

        messagingTemplate.convertAndSend(
                "/topic/game/" + req.getRoomId(),
                GameMapper.toDTO(engine, room)
        );
    }

    private int resolvePlayerIndexByUser(RoomModel room, String userId) {
        if (room.getHostId().equals(userId)) {
            return 0; // player 1
        }

        if (room.getPlayers().containsKey(userId)) {
            return 1; // player 2
        }

        throw new RuntimeException("User is not in this room");
    }

    private void validateUserOwnsCurrentTurn(RoomModel room, GameEngine engine, String userId) {
        int userPlayerIndex = resolvePlayerIndexByUser(room, userId);
        int currentPlayerIndex = engine.getState().getCurrentPlayerIndex();

        if (userPlayerIndex != currentPlayerIndex) {
            throw new RuntimeException("It is not your turn");
        }
    }
}