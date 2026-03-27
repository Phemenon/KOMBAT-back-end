package com.kombat.backend.service;

import com.kombat.backend.config.ConfigReader;
import com.kombat.backend.config.GameConfig;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Getter
@Service
public class ConfigService {

    private GameConfig defaultConfig;

    @PostConstruct
    public void init() {
        try {
            // โหลด file จาก resources/game-config/default.txt
            ClassPathResource resource =
                    new ClassPathResource("game-config/default.txt");

            // ❗ แปลงเป็น temp file path (เพราะ ConfigReader ต้องการ String path)
            var tempFile = Files.createTempFile("config", ".txt");

            try (InputStream is = resource.getInputStream()) {
                Files.copy(is, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            // ใช้ path ที่ได้
            defaultConfig = ConfigReader.load(tempFile.toString());

        } catch (IOException e) {
            throw new RuntimeException("Failed to load config", e);
        }
    }
}