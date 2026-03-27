package com.kombat.backend.service;

import com.kombat.backend.config.GameConfig;
import com.kombat.backend.dto.UpdateConfigRequest;
import com.kombat.backend.game.Engine.GameState.Mode_State.GameMode;
import com.kombat.backend.model.PlayerModel;
import com.kombat.backend.model.RoomModel;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RoomService {

    private final ConfigService configService;
    private final MinionSelectService minionSelectService;
    private final Map<String, RoomModel> rooms = new ConcurrentHashMap<>();

    public RoomService(
            ConfigService configService,
            @Lazy MinionSelectService minionSelectService
    ) {
        this.configService = configService;
        this.minionSelectService = minionSelectService;
    }

    public RoomModel createRoom(String hostId, String userName, GameMode mode) {
        String roomId = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        GameConfig config = configService.getDefaultConfig().copy();

        RoomModel room = new RoomModel(roomId, hostId, config, mode);
        room.getPlayers().put(hostId, new PlayerModel(hostId, userName));

        rooms.put(roomId, room);

        return room;
    }

    public RoomModel joinRoom(String roomId, String userId, String userName) {
        RoomModel room = rooms.get(roomId);

        if (room == null) {
            throw new RuntimeException("Room not found");
        }

        if (room.isGameStarted()) {
            throw new RuntimeException("Game already started");
        }

        if (room.getPlayers().size() >= 2) {
            throw new RuntimeException("Room is full");
        }

        if (!room.getPlayers().containsKey(userId)) {
            room.getPlayers().put(userId, new PlayerModel(userId, userName));
        }

        return room;
    }

    public RoomModel getRoom(String roomId) {
        System.out.println("GET ROOM = " + roomId);
        System.out.println("ROOM KEYS = " + rooms.keySet());

        RoomModel room = rooms.get(roomId);

        if (room == null) {
            throw new RuntimeException("Room not found");
        }

        return room;
    }

    public RoomModel toggleReady(String roomId, String userId) {
        RoomModel room = rooms.get(roomId);

        if (room == null) {
            throw new RuntimeException("Room not found");
        }

        if (room.isGameStarted()) {
            throw new RuntimeException("Game already started");
        }

        PlayerModel player = room.getPlayers().get(userId);

        if (player == null) {
            throw new RuntimeException("Player not found");
        }

        player.setReady(!player.isReady());

        GameMode mode = room.getMode();

        boolean canStart = false;

        if (mode == GameMode.DUEL) {
            canStart = room.getPlayers().size() == 2 &&
                    room.getPlayers().values().stream().allMatch(PlayerModel::isReady);
        } else {
            //  SOLITAIRE + AUTO ใช้แค่ host คนเดียว
            PlayerModel host = room.getPlayers().get(room.getHostId());

            canStart = !room.getPlayers().isEmpty() &&
                    host != null &&
                    host.isReady();
        }

        if (canStart) {
            room.setGameStarted(true);
            minionSelectService.initializeFromRoom(roomId);
        }

        return room;
    }

    public RoomModel updateConfig(String roomId, UpdateConfigRequest request) {
        RoomModel room = rooms.get(roomId);

        if (room == null) {
            throw new RuntimeException("Room not found");
        }

        if (room.isGameStarted()) {
            throw new RuntimeException("Cannot edit config after game started");
        }

        if (!room.getHostId().equals(request.getUserId())) {
            throw new RuntimeException("Only host can edit config");
        }

        GameConfig config = room.getConfig();

        if (request.getSpawnCost() != null) {
            config.setSpawnCost(request.getSpawnCost());
        }

        if (request.getHexPurchaseCost() != null) {
            config.setHexPurchaseCost(request.getHexPurchaseCost());
        }

        if (request.getInitBudget() != null) {
            config.setInitBudget(request.getInitBudget());
        }

        if (request.getTurnBudget() != null) {
            config.setTurnBudget(request.getTurnBudget());
        }

        if (request.getMaxBudget() != null) {
            config.setMaxBudget(request.getMaxBudget());
        }

        if (request.getBaseInterestPct() != null) {
            config.setBaseInterestPct(request.getBaseInterestPct());
        }

        if (request.getInitHp() != null) {
            config.setInitHp(request.getInitHp());
        }

        if (request.getMaxSpawns() != null) {
            config.setMaxSpawns(request.getMaxSpawns());
        }

        if (request.getMaxTurns() != null) {
            config.setMaxTurns(request.getMaxTurns());
        }

        return room;
    }

    public RoomModel startGame(String roomId, String userId) {
        RoomModel room = rooms.get(roomId);

        if (room == null) {
            throw new RuntimeException("Room not found");
        }

        if (!room.getHostId().equals(userId)) {
            throw new RuntimeException("Only host can start game");
        }

        GameMode mode = room.getMode();

        if (mode == GameMode.DUEL) {
            if (room.getPlayers().size() < 2) {
                throw new RuntimeException("Need 2 players to start");
            }

            boolean guestReady = room.getPlayers()
                    .values()
                    .stream()
                    .filter(player -> !player.getUserId().equals(room.getHostId()))
                    .allMatch(PlayerModel::isReady);

            if (!guestReady) {
                throw new RuntimeException("Guest must be ready");
            }
        } else {
            PlayerModel host = room.getPlayers().get(room.getHostId());
            if (host == null || !host.isReady()) {
                throw new RuntimeException("Host must be ready");
            }
        }

        room.setGameStarted(true);
        minionSelectService.initializeFromRoom(roomId);
        return room;
    }
}