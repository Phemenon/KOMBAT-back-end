package com.kombat.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ToggleReadyRequest {
    private String roomId;
    private String userId;
}
