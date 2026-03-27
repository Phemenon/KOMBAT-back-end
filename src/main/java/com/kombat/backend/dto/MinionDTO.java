package com.kombat.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MinionDTO {
    private int row;
    private int col;
    private int owner;
    private long hp;
    private String type;
}