package com.kombat.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ValidateMinionStrategyRequest {
    private String userId;
    private String minionType;
    private String strategySource;
}