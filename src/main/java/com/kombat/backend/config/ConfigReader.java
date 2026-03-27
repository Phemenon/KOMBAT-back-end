package com.kombat.backend.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ConfigReader {

    private static class ConfigValues {

        long spawnCost;
        boolean hasSpawnCost;

        long hexPurchaseCost;
        boolean hasHexPurchaseCost;

        long initBudget;
        boolean hasInitBudget;

        long turnBudget;
        boolean hasTurnBudget;

        long maxBudget;
        boolean hasMaxBudget;

        long baseInterestPct;
        boolean hasBaseInterestPct;

        long initHp;
        boolean hasInitHp;

        int maxSpawns;
        boolean hasMaxSpawns;

        int maxTurns;
        boolean hasMaxTurns;
    }

    public static GameConfig load(String path) throws IOException {

        ConfigValues values = new ConfigValues();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {

            String line;
            int lineNumber = 0;

            while ((line = br.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("=", 2);
                if (parts.length != 2) {
                    throw new IllegalArgumentException(
                            "Config error at " + path +
                                    " line " + lineNumber +
                                    ": invalid format (expected key = value)\n> " + line
                    );
                }

                String key = parts[0].trim();
                String value = parts[1].trim();

                try {
                    assign(values, key, value, path, lineNumber, line);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Config error at " + path +
                                    " line " + lineNumber +
                                    ": invalid number for key '" + key + "'\n> " + line
                    );
                }
            }

            validate(values, path);

            return new GameConfig(
                    values.spawnCost,
                    values.hexPurchaseCost,
                    values.initBudget,
                    values.turnBudget,
                    values.maxBudget,
                    values.baseInterestPct,
                    values.initHp,
                    values.maxSpawns,
                    values.maxTurns
            );
        }
    }

    private static void validate(ConfigValues values, String path) {

        if (!values.hasSpawnCost)
            throw new IllegalArgumentException("Missing required config: spawn_cost in " + path);

        if (!values.hasInitBudget)
            throw new IllegalArgumentException("Missing required config: init_budget in " + path);

        if (!values.hasMaxTurns)
            throw new IllegalArgumentException("Missing required config: max_turns in " + path);
    }

    private static void assign(ConfigValues values,
                               String key,
                               String value,
                               String path,
                               int lineNumber,
                               String rawLine) {

        switch (key) {

            case "spawn_cost" -> {
                if (values.hasSpawnCost)
                    throw duplicate(path, lineNumber, key, rawLine);
                values.spawnCost = Long.parseLong(value);
                values.hasSpawnCost = true;
            }

            case "hex_purchase_cost" -> {
                if (values.hasHexPurchaseCost)
                    throw duplicate(path, lineNumber, key, rawLine);
                values.hexPurchaseCost = Long.parseLong(value);
                values.hasHexPurchaseCost = true;
            }

            case "init_budget" -> {
                if (values.hasInitBudget)
                    throw duplicate(path, lineNumber, key, rawLine);
                values.initBudget = Long.parseLong(value);
                values.hasInitBudget = true;
            }

            case "turn_budget" -> {
                if (values.hasTurnBudget)
                    throw duplicate(path, lineNumber, key, rawLine);
                values.turnBudget = Long.parseLong(value);
                values.hasTurnBudget = true;
            }

            case "max_budget" -> {
                if (values.hasMaxBudget)
                    throw duplicate(path, lineNumber, key, rawLine);
                values.maxBudget = Long.parseLong(value);
                values.hasMaxBudget = true;
            }

            case "interest_pct" -> {
                if (values.hasBaseInterestPct)
                    throw duplicate(path, lineNumber, key, rawLine);
                values.baseInterestPct = Long.parseLong(value);
                values.hasBaseInterestPct = true;
            }

            case "init_hp" -> {
                if (values.hasInitHp)
                    throw duplicate(path, lineNumber, key, rawLine);
                values.initHp = Long.parseLong(value);
                values.hasInitHp = true;
            }

            case "max_spawns" -> {
                if (values.hasMaxSpawns)
                    throw duplicate(path, lineNumber, key, rawLine);
                values.maxSpawns = Integer.parseInt(value);
                values.hasMaxSpawns = true;
            }

            case "max_turns" -> {
                if (values.hasMaxTurns)
                    throw duplicate(path, lineNumber, key, rawLine);
                values.maxTurns = Integer.parseInt(value);
                values.hasMaxTurns = true;
            }

            default -> throw new IllegalArgumentException(
                    "Config error at " + path +
                            " line " + lineNumber +
                            ": unknown config key '" + key + "'\n> " + rawLine
            );
        }
    }

    private static IllegalArgumentException duplicate(String path,
                                                      int lineNumber,
                                                      String key,
                                                      String rawLine) {

        return new IllegalArgumentException(
                "Config error at " + path +
                        " line " + lineNumber +
                        ": duplicate key '" + key + "'\n> " + rawLine
        );
    }
}