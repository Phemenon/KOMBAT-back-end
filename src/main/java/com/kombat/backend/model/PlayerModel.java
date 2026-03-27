package com.kombat.backend.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerModel {
    private String userId;
    private String userName;
    private boolean ready;

    public PlayerModel(String userId, String userName) {
        this.userId = userId;
        this.userName = userName;
        this.ready = false;
    }
}