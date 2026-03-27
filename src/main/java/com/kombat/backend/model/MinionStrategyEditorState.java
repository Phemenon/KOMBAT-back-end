package com.kombat.backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MinionStrategyEditorState {
    private String minionType;
    private String defaultStrategy;
    private String currentStrategy;
    private boolean valid;
    private String message;
}