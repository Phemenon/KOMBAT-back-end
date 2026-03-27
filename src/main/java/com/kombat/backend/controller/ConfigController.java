package com.kombat.backend.controller;

import com.kombat.backend.config.GameConfig;
import com.kombat.backend.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class ConfigController {
    private final ConfigService configService;

    @GetMapping("/default")
    public GameConfig getDefault() {
        return configService.getDefaultConfig();
    }
}
