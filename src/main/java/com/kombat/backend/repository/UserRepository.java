package com.kombat.backend.repository;

import com.kombat.backend.model.UserModel;

import java.util.UUID;

public interface UserRepository {
    UUID addUser(String name);
    UserModel getUserByID(UUID userID);
}
