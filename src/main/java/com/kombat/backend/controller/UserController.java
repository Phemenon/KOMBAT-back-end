package com.kombat.backend.controller;

import com.kombat.backend.dto.CreateUser;
import com.kombat.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;

    @PostMapping("/create")
    public UUID createUser(@RequestBody CreateUser createUser) { return userRepository.addUser(createUser.getName()); }
}
