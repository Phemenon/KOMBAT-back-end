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
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GameFactory {

    private static final List<String> BASE_TYPE_NAMES = List.of(
            "Factory",
            "Calmer",
            "Berserker",
            "Bomber",
            "Gambler"
    );

    // ===== โหลด TypeMinion =====
    private static List<TypeMinion> loadAllTypes(
            Map<String, Long> defendMap,
            Map<String, String> strategyMap
    ) throws IOException, InvalidExpressionException {

        List<TypeMinion> list = new ArrayList<>();

        for (String name : BASE_TYPE_NAMES) {

            long defend = defendMap != null ? defendMap.getOrDefault(name, 0L) : 0;

            String strategySource;

            // 🔥 priority: custom strategy (จาก minion select)
            if (strategyMap != null && strategyMap.containsKey(name)) {
                strategySource = strategyMap.get(name);
            } else {
                // fallback → load จาก resource
                String path = "StrategyFile/" + name + ".txt";
                ClassPathResource resource = new ClassPathResource(path);

                if (!resource.exists()) {
                    throw new RuntimeException("Strategy file not found: " + path);
                }

                try (InputStream is = resource.getInputStream()) {
                    strategySource = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                }
            }

            Strategy strategy = StrategyEvaluator.compile(strategySource);

            list.add(new TypeMinion(name, defend, strategy));
        }

        return list;
    }

    // ===== ใช้ตอน start game =====
    public static GameEngine createFromRoomConfig(
            GameConfig config,
            GameMode mode,
            Set<String> selectedTypes,
            Map<String, Long> defendMap,
            Map<String, String> strategyMap
    ) throws IOException {

        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }

        Board board = new Board(8, 8);

        List<TypeMinion> allTypes;
        try {
            allTypes = loadAllTypes(defendMap, strategyMap);
        } catch (InvalidExpressionException e) {
            throw new RuntimeException("Strategy compile failed", e);
        }

        Player p1 = new Player(1, config.getInitBudget(), config.getMaxSpawns());
        Player p2 = new Player(2, config.getInitBudget(), config.getMaxSpawns());

        List<TypeMinion> selected = ForSpringFactory.selectTypes(allTypes, new ArrayList<>(selectedTypes));

        p1.setAllowedTypes(selected);
        p2.setAllowedTypes(selected);

        return new GameEngine(
                new GameState(board, p1, p2, mode),
                config
        );
    }
}