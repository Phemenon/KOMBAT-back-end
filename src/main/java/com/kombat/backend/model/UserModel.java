package com.kombat.backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
public class UserModel {
    private UUID userID;
    private String username;
    private boolean ready;
}
