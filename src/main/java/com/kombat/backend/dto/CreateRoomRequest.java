package com.kombat.backend.dto;

import lombok.Data;

@Data
public class CreateRoomRequest {
    private String hostId;
    private String userName;
    private String mode;
}