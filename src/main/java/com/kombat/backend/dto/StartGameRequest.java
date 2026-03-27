package com.kombat.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartGameRequest {
    private String roomId;
    private String userId;
}