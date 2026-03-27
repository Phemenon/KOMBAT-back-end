package com.kombat.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SpawnableHexDTO {
    private int row;
    private int col;
    private boolean spawnableP1;
    private boolean spawnableP2;
}