package com.kombat.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GameDTO {
    private int currentPlayer;
    private int turn;
    private List<MinionDTO> minions;

    private long player1Budget;
    private long player2Budget;

    private long player1TotalHp;
    private long player2TotalHp;

    private long player1SpawnsLeft;
    private long player2SpawnsLeft;

    private String result;
    private List<String> events;

    private List<String> currentPlayerAllowedTypes;

    private long hexPurchaseCost;
    private List<SpawnableHexDTO> spawnableHexes;
    private boolean currentPlayerBoughtHexThisTurn;
    private boolean currentPlayerSpawnedThisTurn;
    private String hostId;
    private String mode;
}