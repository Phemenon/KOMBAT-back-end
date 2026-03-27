package com.kombat.backend.service;

import com.kombat.backend.config.GameConfig;
import com.kombat.backend.game.DefaultProvider;
import com.kombat.backend.game.Engine.GameEngine;
import com.kombat.backend.game.Engine.GameState.Mode_State.GameMode;
import com.kombat.backend.game.Exception.InvalidExpressionException;
import com.kombat.backend.game.ForSpringFactory;
import com.kombat.backend.model.MinionPlayerState;
import com.kombat.backend.model.MinionSelectState;
import com.kombat.backend.model.MinionStrategyEditorState;
import com.kombat.backend.model.PlayerModel;
import com.kombat.backend.model.RoomModel;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class MinionSelectService {

    /**
     * รายชื่อ minion type ที่ระบบรองรับจริง
     * ต้องตรงกับชื่อที่ใช้ใน GameFactory และ strategy txt files
     */
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "Factory",
            "Calmer",
            "Berserker",
            "Bomber",
            "Gambler"
    );

    /**
     * ใช้ @Lazy กัน circular dependency
     * เพราะ RoomService เรียก initializeFromRoom()
     * และ service นี้ก็ต้องเรียก roomService.getRoom()
     */
    private final @Lazy RoomService roomService;

    /**
     * ใช้อ่าน default strategy จาก resource txt
     */
    private final StrategyTemplateService strategyTemplateService;

    /**
     * ใช้ validate strategy ด้วย parser
     */
    private final StrategyValidationService strategyValidationService;

    /**
     * เก็บ GameEngine runtime แยกตาม roomId
     */
    private final GameSessionService gameSessionService;

    /**
     * roomId -> minion select state
     */
    private final Map<String, MinionSelectState> selectStateByRoomId = new ConcurrentHashMap<>();

    /**
     * สร้าง state เริ่มต้นของ minion-select จาก room ปัจจุบัน
     * ถ้ามีอยู่แล้วจะคืนตัวเดิม
     */
    public MinionSelectState initializeFromRoom(String roomId) {
        RoomModel room = roomService.getRoom(roomId);

        return selectStateByRoomId.computeIfAbsent(roomId, ignored -> {
            MinionSelectState state = new MinionSelectState();

            // ตั้งค่า room พื้นฐาน
            state.setRoomId(room.getRoomId());
            state.setHostId(room.getHostId());
            state.setGameStarted(false);

            // แปลง players จาก room ไปเป็น players ของหน้า minion-select
            Map<String, MinionPlayerState> playerStates = new LinkedHashMap<>();
            for (PlayerModel player : room.getPlayers().values()) {
                playerStates.put(
                        player.getUserId(),
                        new MinionPlayerState(
                                player.getUserId(),
                                player.getUserName(),
                                false
                        )
                );
            }
            state.setPlayers(playerStates);

            // โหลด default strategy ทั้งหมดจาก resource
            Map<String, String> defaultStrategies = strategyTemplateService.loadAllDefaultStrategies();
            state.setDefaultStrategyMap(defaultStrategies);

            // current strategy ตอนเริ่มต้น = default strategy
            state.setCurrentStrategyMap(new LinkedHashMap<>(defaultStrategies));

            // defense factor
            Map<String, Long> defaultDefenseFactors =
                    new LinkedHashMap<>(DefaultProvider.defaultDefendFactorMap());

            state.setDefaultDefenseFactorMap(defaultDefenseFactors);
            state.setCurrentDefenseFactorMap(new LinkedHashMap<>(defaultDefenseFactors));

            return state;
        });
    }

    /**
     * ดึง state ของห้อง
     */
    public MinionSelectState getState(String roomId) {
        MinionSelectState state = selectStateByRoomId.get(roomId);

        if (state == null) {
            throw new RuntimeException("Minion select state not found");
        }

        return state;
    }

    /**
     * sync state ตอน frontend เข้าหน้า select หรือ refresh หน้า
     */
    public MinionSelectState sync(String roomId, String userId) {
        MinionSelectState state = selectStateByRoomId.get(roomId);

        // ถ้ายังไม่มี state ให้ init จาก room ก่อน
        if (state == null) {
            state = initializeFromRoom(roomId);
        }

        validatePlayerExists(state, userId);

        return state;
    }

    /**
     * อัปเดต shared minion types ของห้อง
     * ถ้า selection เปลี่ยน จะ reset ready ของทุกคน
     */
    public MinionSelectState updateSelection(String roomId, String userId, Set<String> selectedTypes) {
        MinionSelectState state = sync(roomId, userId);

        // ห้ามแก้หลังเกมเริ่ม
        state.ensureGameNotStarted();

        validateSelection(selectedTypes);

        // ถ้า selection เดิมกับใหม่ไม่เหมือนกัน ค่อย reset ready
        boolean changed = !state.getSelectedTypes().equals(selectedTypes);

        state.setSelectedTypes(selectedTypes);

        if (changed) {
            // มีการเปลี่ยน shared types ต้องให้ทุกคน ready ใหม่
            state.resetAllReady();
        }

        return state;
    }

    /**
     * toggle ready / unready ของผู้เล่น
     * ถ้า ready ครบและมี minion ที่เลือกแล้ว อาจ start game ต่อได้ใน service/controller ชั้นบน
     */
    public MinionSelectState toggleReady(String roomId, String userId) {
        MinionSelectState state = sync(roomId, userId);

        state.ensureGameNotStarted();

        if (state.getSelectedCount() < 1) {
            throw new RuntimeException("Please select at least 1 minion type first");
        }

        MinionPlayerState player = state.getPlayer(userId);
        if (player == null) {
            throw new RuntimeException("Player not found");
        }

        player.setReady(!player.isReady());

        if (state.canStartGame()) {
            return startGameIfReady(roomId);
        }

        return state;
    }

    /**
     * เปิด editor ของ minion type ที่เลือก
     * ใช้ current strategy เป็น draft ปัจจุบัน
     */
    public MinionStrategyEditorState openStrategyEditor(String roomId, String userId, String minionType) {
        MinionSelectState state = sync(roomId, userId);

        state.ensureGameNotStarted();
        validateType(minionType);

        String defaultStrategy = state.getDefaultStrategy(minionType);
        String currentStrategy = state.getEffectiveStrategy(minionType);

        return new MinionStrategyEditorState(
                minionType,
                defaultStrategy,
                currentStrategy,
                false,
                ""
        );
    }

    /**
     * กดปุ่ม default ใน editor
     * คืนค่ากลับเป็น strategy จาก resource
     *
     * หมายเหตุ:
     * method นี้ยังไม่ confirm ลง state
     * แค่ส่งค่า default กลับไปให้ frontend แสดงใน textarea
     */
    public MinionStrategyEditorState resetStrategyToDefault(String roomId, String userId, String minionType) {
        MinionSelectState state = sync(roomId, userId);

        state.ensureGameNotStarted();
        validateType(minionType);

        String defaultStrategy = state.getDefaultStrategy(minionType);

        return new MinionStrategyEditorState(
                minionType,
                defaultStrategy,
                defaultStrategy,
                false,
                "Reset to default"
        );
    }

    /**
     * validate strategy ที่ user พิมพ์
     * ถ้าผ่านจะคืน valid=true ให้ frontend เปิดปุ่ม Confirm
     *
     * หมายเหตุ:
     * ยังไม่เขียนลง state จริง จนกว่าจะกด Confirm
     */
    public MinionStrategyEditorState validateStrategy(
            String roomId,
            String userId,
            String minionType,
            String strategySource
    ) {
        MinionSelectState state = sync(roomId, userId);

        state.ensureGameNotStarted();
        validateType(minionType);

        // ใช้ parser ตรวจ syntax/exception
        strategyValidationService.validate(strategySource);

        return new MinionStrategyEditorState(
                minionType,
                state.getDefaultStrategy(minionType),
                strategySource,
                true,
                "Strategy is valid"
        );
    }

    /**
     * confirm strategy ใหม่ของ minion type นี้
     * จะ validate ซ้ำที่ backend เพื่อกัน frontend ส่งมั่ว
     * และ reset ready ของทุกคนหลังแก้สำเร็จ
     */
    public MinionSelectState confirmStrategy(
            String roomId,
            String userId,
            String minionType,
            String strategySource
    ) {
        MinionSelectState state = sync(roomId, userId);

        state.ensureGameNotStarted();
        validateType(minionType);

        // กัน strategy ว่าง
        if (strategySource == null || strategySource.isBlank()) {
            throw new RuntimeException("Strategy must not be empty");
        }

        // validate ซ้ำอีกรอบที่ backend
        strategyValidationService.validate(strategySource);

        // บันทึก strategy ใหม่ของ type นี้
        state.setStrategy(minionType, strategySource);

        // มีการแก้ strategy ต้องให้ทุกคน ready ใหม่
        state.resetAllReady();

        return state;
    }

    /**
     * สร้าง GameEngine จาก state ของห้อง
     * ใช้ room config + selected types + current strategy map
     */
    public GameEngine createGameEngine(String roomId) {
        MinionSelectState state = getState(roomId);

        if (!state.canStartGame()) {
            throw new RuntimeException("Minion select state is not ready to start game");
        }

        RoomModel room = roomService.getRoom(roomId);
        GameConfig roomConfig = room.getConfig();

        try {
            return ForSpringFactory.create(
                    roomConfig,
                    room.getMode(),
                    state.getSelectedTypes(),
                    state.getCurrentDefenseFactorMap(),
                    state.getCurrentStrategyMap()
            );
        } catch (IOException | InvalidExpressionException e) {
            throw new RuntimeException("Cannot create GameEngine", e);
        }
    }

    /**
     * ใช้ตอนจะเริ่มเกมจริง
     * ถ้ายังไม่มี engine ของ room นี้ จะสร้างและเก็บไว้
     * แล้ว mark gameStarted=true
     */
    public MinionSelectState startGameIfReady(String roomId) {
        MinionSelectState state = getState(roomId);

        state.ensureGameNotStarted();

        if (!state.canStartGame()) {
            throw new RuntimeException("Players are not ready or no minion types selected");
        }

        // ถ้ายังไม่มี engine ค่อยสร้าง
        if (!gameSessionService.hasEngine(roomId)) {
            GameEngine engine = createGameEngine(roomId);
            gameSessionService.putEngine(roomId, engine);
        }

        state.setGameStarted(true);
        return state;
    }

    /**
     * ลบ state ของห้องเมื่อจบเกมหรือออกจาก room
     */
    public void removeState(String roomId) {
        selectStateByRoomId.remove(roomId);
    }

    /**
     * ตรวจว่าผู้เล่นอยู่ในห้องนี้จริง
     */
    private void validatePlayerExists(MinionSelectState state, String userId) {
        if (userId == null || userId.isBlank()) {
            throw new RuntimeException("userId is required");
        }

        if (!state.hasPlayer(userId)) {
            throw new RuntimeException("Player not found in minion select room");
        }
    }

    /**
     * ตรวจว่า minion type ที่ส่งมาเป็น type ที่รองรับจริง
     */
    private void validateType(String minionType) {
        if (minionType == null || minionType.isBlank()) {
            throw new RuntimeException("Minion type is required");
        }

        if (!ALLOWED_TYPES.contains(minionType)) {
            throw new RuntimeException("Unknown minion type: " + minionType);
        }
    }

    /**
     * ตรวจ selected types
     * - ต้องไม่ null
     * - ต้องมี 1-5 ชนิด
     * - ทุกตัวต้องอยู่ใน ALLOWED_TYPES
     */
    private void validateSelection(Set<String> selectedTypes) {
        if (selectedTypes == null) {
            throw new RuntimeException("Selected minion types must not be null");
        }

        if (selectedTypes.isEmpty() || selectedTypes.size() > 5) {
            throw new RuntimeException("Selected minion types must contain 1-5 entries");
        }

        for (String type : selectedTypes) {
            validateType(type);
        }
    }

    public MinionSelectState confirmDefenseFactor(
            String roomId,
            String userId,
            String minionType,
            long defenseFactor
    ) {
        MinionSelectState state = sync(roomId, userId);

        state.ensureGameNotStarted();
        validateType(minionType);

        if (defenseFactor < 0) {
            throw new RuntimeException("Defense factor must be >= 0");
        }

        state.setDefenseFactor(minionType, defenseFactor);

        // มีการแก้ setup ต้องให้ทุกคน ready ใหม่
        state.resetAllReady();

        return state;
    }

    public MinionSelectState resetDefenseFactorToDefault(
            String roomId,
            String userId,
            String minionType
    ) {
        MinionSelectState state = sync(roomId, userId);

        state.ensureGameNotStarted();
        validateType(minionType);

        long defaultDefense = state.getDefaultDefenseFactor(minionType);
        state.setDefenseFactor(minionType, defaultDefense);
        state.resetAllReady();

        return state;
    }
}