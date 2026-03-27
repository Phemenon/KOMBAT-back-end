package com.kombat.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinRoomRequest {
    private String roomId;
    private String userId;
    private String userName;
}