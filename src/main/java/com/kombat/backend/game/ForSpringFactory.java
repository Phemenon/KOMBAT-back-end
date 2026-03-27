package com.kombat.backend.game;



import com.kombat.backend.config.GameConfig;
import com.kombat.backend.game.Engine.GameEngine;
import com.kombat.backend.game.Engine.GameState.Board_Direction.Board;
import com.kombat.backend.game.Engine.GameState.Mode_State.GameMode;
import com.kombat.backend.game.Engine.GameState.Mode_State.GameState;
import com.kombat.backend.game.Engine.StrategyEvaluator;
import com.kombat.backend.game.Exception.InvalidExpressionException;
import com.kombat.backend.game.Parser_Eval.ASTNode_Eval.Strategy;
import com.kombat.backend.game.Player_Minions.Player;
import com.kombat.backend.game.Player_Minions.TypeMinion;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Factory สำหรับสร้าง GameEngine จากข้อมูลที่มาจาก Spring Layer
 * หน้าที่:
 *  - แปลง config และ input จากภายนอก (เช่น controller)
 *    ให้กลายเป็น object ใน Core Layer
 *  - compile strategy source ให้เป็น Strategy AST
 *  - validate type selection
 * <p>
 * คลาสนี้ถือเป็น Boundary ระหว่าง Spring กับ Backend Core
 * <p>
 * หมายเหตุ:
 *  - ไม่มี state ภายใน (stateless)
 *  - ทุก method เป็น pure factory method
 */
public class ForSpringFactory {


    /**
     * โหลดและสร้าง TypeMinion ทั้งหมดโดยอิงจากค่า default
     * และ apply override ที่ส่งมาจากภายนอก (เช่น Spring)
     * <p>
     * ลำดับความสำคัญของค่า:
     * Strategy: defaultStrategySourceMap() → strategyOverrideMap
     * DefendFactor: defaultDefendFactorMap() → defendOverrideMap
     * <p>
     * หาก overrideMap เป็น null จะใช้ค่า default ทั้งหมด
     *
     * @param defendOverrideMap
     *        map ของค่า defend ต่อ type ที่ต้องการ override
     *        (อาจเป็น null เพื่อใช้ค่า default ทั้งหมด)
     *
     * @param strategyOverrideMap
     *        map ของชื่อ type → source code ของ strategy
     *        ที่ต้องการ override (อาจเป็น null)
     *
     * @return List ของ TypeMinion ที่ถูกสร้างและ compile สำเร็จครบทุก type
     *
     * @throws IllegalArgumentException
     *         หาก overrideMap มี key ที่ไม่มีอยู่ใน default type definitions
     *
     * @throws InvalidExpressionException
     *         หาก strategy ใด compile ไม่สำเร็จ
     *
     * @throws IOException
     *         หากเกิดข้อผิดพลาดระหว่างโหลด default strategy
     *
     */
    private static List<TypeMinion> loadAllTypes(
            Map<String, Long> defendOverrideMap,
            Map<String, String> strategyOverrideMap
    ) throws InvalidExpressionException, IOException {

        //  Load defaults
        Map<String, Long> finalDefendMap =
                new HashMap<>(DefaultProvider.defaultDefendFactorMap());

        Map<String, String> finalStrategyMap =
                new HashMap<>(DefaultProvider.defaultStrategySourceMap());

        //  Apply strategy overrides
        if (strategyOverrideMap != null) {

            for (String key : strategyOverrideMap.keySet()) {
                if (!finalStrategyMap.containsKey(key)) {
                    throw new IllegalArgumentException(
                            "Strategy provided for unknown type: " + key
                    );
                }
            }

            finalStrategyMap.putAll(strategyOverrideMap);
        }

        // Apply to defend factor overrides
        if (defendOverrideMap != null) {

            for (String key : defendOverrideMap.keySet()) {
                if (!finalStrategyMap.containsKey(key)) {
                    throw new IllegalArgumentException(
                            "Defend provided for unknown type: " + key
                    );
                }
            }

            finalDefendMap.putAll(defendOverrideMap);
        }

        //  Build Types
        List<TypeMinion> list = new ArrayList<>();

        for (String name : finalStrategyMap.keySet()) {

            long defend = finalDefendMap.getOrDefault(name, 0L);
            Strategy strategy = StrategyEvaluator.compile(
                    finalStrategyMap.get(name)
            );

            list.add(new TypeMinion(name, defend, strategy));
        }

        return list;
    }

    /**
     * เลือก TypeMinion จากรายชื่อทั้งหมดตามชื่อที่ผู้เล่นเลือก
     *
     * @param allTypes รายการ type ทั้งหมด
     * @param chosenNames รายชื่อ type ที่ผู้เล่นเลือก
     *
     * @return list ของ TypeMinion ที่ตรงกับ chosenNames
     *
     * @precondition
     *  - chosenNames ต้องไม่ว่าง
     *  - ห้ามมี duplicate
     *  - จำนวนต้องไม่เกิน 5
     *  - ทุกชื่อใน chosenNames ต้องมีอยู่ใน allTypes
     *
     * @throws IllegalArgumentException หากผิดเงื่อนไขใด ๆ
     *
     * @sideEffect ไม่มี
     */
    public static List<TypeMinion> selectTypes(
            List<TypeMinion> allTypes,
            Collection<String> chosenNames) {

        if (chosenNames == null || chosenNames.isEmpty()) {
            throw new IllegalArgumentException(
                    "Must select at least 1 type"
            );
        }

        if (new HashSet<>(chosenNames).size() != chosenNames.size()) {
            throw new IllegalArgumentException(
                    "Duplicate type selection is not allowed"
            );
        }

        if (chosenNames.size() > 5) {
            throw new IllegalArgumentException(
                    "Too many types selected"
            );
        }

        Map<String, TypeMinion> map = allTypes.stream()
                .collect(Collectors.toMap(
                        TypeMinion::name, type -> type));

        List<TypeMinion> result = new ArrayList<>();

        for (String name : chosenNames) {

            TypeMinion type = map.get(name);

            if (type == null) {
                throw new IllegalArgumentException(
                        "Unknown type: " + name
                );
            }

            result.add(type);
        }

        return result;
    }


    // ===== Factory Method หลัก =====
    /**
     * create จะสร้าง GameEngine สำหรับใช้งานใน Spring Layer
     * Method นี้ทำหน้าที่เป็น Boundary ระหว่าง Spring กับ Core Engine
     * โดยจะ:
     * สร้าง Board จาก GameConfig
     * โหลด TypeMinion ทั้งหมดจาก default + override
     * สร้าง Player ทั้งสองฝ่าย
     * กำหนด allowed types ให้ผู้เล่น
     * ประกอบ GameState และ GameEngine
     * <p>
     * ลำดับความสำคัญของค่า strategy/defend:
     * DefaultProvider → Override จาก parameter
     *
     * @param config ค่าคอนฟิกหลักของเกม (เช็คข้อมูล Class ได้ที่ Backend.Config.GameConfig)
     * @param mode โหมดของเกม (ENUM Backend.GameState.Mode_State.GameMode DUEL/SOLITAIRE/AUTO)
     * @param sharedChoices รายชื่อ type ที่ผู้เล่นทั้งสองฝ่ายสามารถเลือกใช้ได้
     * @param defendOverrideMap map สำหรับ override ค่า defend (null -> use default)
     * @param strategyOverrideMap map สำหรับ override strategy source (null -> use default)
     *
     * @return GameEngine ที่พร้อมเริ่มเกม
     *
     * @throws InvalidExpressionException หาก strategy ใด compile ไม่สำเร็จ
     * @throws IOException หากเกิดข้อผิดพลาดระหว่างโหลด default strategy
     */
    public static GameEngine create(
            GameConfig config,
            GameMode mode,
            Collection<String> sharedChoices,
            Map<String, Long> defendOverrideMap,
            Map<String, String> strategyOverrideMap
    ) throws InvalidExpressionException, IOException {

        Board board = new Board(8, 8);

        List<TypeMinion> allTypes =
                loadAllTypes(defendOverrideMap, strategyOverrideMap);

        Player p1 = new Player(1, config.getInitBudget(), config.getMaxSpawns());
        Player p2 = new Player(2, config.getInitBudget(), config.getMaxSpawns());

        List<TypeMinion> selected =
                selectTypes(allTypes, sharedChoices);

        p1.setAllowedTypes(selected);
        p2.setAllowedTypes(selected);

        return new GameEngine(
                new GameState(board, p1, p2, mode),
                config
        );
    }
}

