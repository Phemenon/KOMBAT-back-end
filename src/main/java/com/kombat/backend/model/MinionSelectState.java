package com.kombat.backend.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
public class MinionSelectState {

    private String roomId;
    private String hostId;
    private boolean gameStarted;

    // ใช้ Set กัน duplicate + รักษา order
    private Set<String> selectedTypes = new LinkedHashSet<>();

    // userId -> playerState
    private Map<String, MinionPlayerState> players = new LinkedHashMap<>();

    // type -> default strategy (from resource)
    private Map<String, String> defaultStrategyMap = new LinkedHashMap<>();

    // type -> current strategy (user edited)
    private Map<String, String> currentStrategyMap = new LinkedHashMap<>();
    private Map<String, Long> defaultDefenseFactorMap = new LinkedHashMap<>();
    private Map<String, Long> currentDefenseFactorMap = new LinkedHashMap<>();


    // =========================
    // 🔹 PLAYER HELPERS
    // =========================

    public boolean isFull() {
        return players.size() >= 2;
    }

    public boolean hasPlayer(String userId) {
        return players.containsKey(userId);
    }

    public MinionPlayerState getPlayer(String userId) {
        return players.get(userId);
    }

    public Collection<MinionPlayerState> getAllPlayers() {
        return players.values();
    }

    public boolean allPlayersReady() {
        return players.size() == 2 &&
                players.values().stream().allMatch(MinionPlayerState::isReady);
    }

    public void resetAllReady() {
        for (MinionPlayerState player : players.values()) {
            player.setReady(false);
        }
    }


    // =========================
    // 🔹 MINION TYPE HELPERS
    // =========================

    public boolean isTypeSelected(String type) {
        return selectedTypes.contains(type);
    }

    public void selectType(String type) {
        selectedTypes.add(type);
    }

    public void unselectType(String type) {
        selectedTypes.remove(type);
    }

    public int getSelectedCount() {
        return selectedTypes.size();
    }

    public long getDefaultDefenseFactor(String minionType) {
        Long value = defaultDefenseFactorMap.get(minionType);
        if (value == null) {
            throw new RuntimeException("Default defense factor not found for: " + minionType);
        }
        return value;
    }

    public long getEffectiveDefenseFactor(String minionType) {
        Long value = currentDefenseFactorMap.get(minionType);
        if (value != null) return value;
        return getDefaultDefenseFactor(minionType);
    }

    public void setDefenseFactor(String minionType, long defenseFactor) {
        currentDefenseFactorMap.put(minionType, defenseFactor);
    }


    // =========================
    // 🔹 STRATEGY HELPERS
    // =========================

    public String getDefaultStrategy(String type) {
        return defaultStrategyMap.get(type);
    }

    public String getCurrentStrategy(String type) {
        return currentStrategyMap.get(type);
    }

    public String getEffectiveStrategy(String type) {
        return currentStrategyMap.getOrDefault(type, defaultStrategyMap.get(type));
    }

    public void setStrategy(String type, String strategy) {
        currentStrategyMap.put(type, strategy);
    }

    public void resetStrategyToDefault(String type) {
        String defaultStrategy = defaultStrategyMap.get(type);
        if (defaultStrategy != null) {
            currentStrategyMap.put(type, defaultStrategy);
        }
    }

    public boolean hasStrategy(String type) {
        return currentStrategyMap.containsKey(type) || defaultStrategyMap.containsKey(type);
    }


    // =========================
    // 🔹 GAME STATE HELPERS
    // =========================

    public void ensureGameNotStarted() {
        if (gameStarted) {
            throw new RuntimeException("Game already started");
        }
    }

    public boolean canStartGame() {
        return selectedTypes != null
                && !selectedTypes.isEmpty()
                && players.values().stream().allMatch(MinionPlayerState::isReady);
    }
}