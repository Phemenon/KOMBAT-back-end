package com.kombat.backend.repository;

import com.kombat.backend.model.UserModel;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Repository
public class MyUserRepository implements UserRepository{
    private final Map<UUID, UserModel> users = new HashMap<>();

    @Override
    public UUID addUser(String name) {
        UserModel userModel = new UserModel(UUID.randomUUID(), name, false);
        users.put(userModel.getUserID(), userModel);
        return userModel.getUserID();
    }

    @Override
    public UserModel getUserByID(UUID userID) {
        return users.get(userID);
    }
}
