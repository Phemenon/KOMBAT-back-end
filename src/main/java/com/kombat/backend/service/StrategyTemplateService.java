package com.kombat.backend.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StrategyTemplateService {

    private static final List<String> BASE_TYPE_NAMES = List.of(
            "Factory",
            "Calmer",
            "Berserker",
            "Bomber",
            "Gambler"
    );

    public String loadDefaultStrategy(String minionType) {
        validateType(minionType);

        String path = "strategyFile/" + minionType + ".txt";

        try {
            ClassPathResource resource = new ClassPathResource(path);

            System.out.println("PATH = " + path);
            System.out.println("EXISTS = " + resource.exists());

            if (!resource.exists()) {
                throw new RuntimeException("Strategy template not found: " + path);
            }

            try (InputStream is = resource.getInputStream()) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }

        } catch (IOException e) {
            throw new RuntimeException("Cannot load strategy template: " + minionType, e);
        }
    }

    public Map<String, String> loadAllDefaultStrategies() {
        Map<String, String> map = new LinkedHashMap<>();

        for (String type : BASE_TYPE_NAMES) {
            map.put(type, loadDefaultStrategy(type));
        }

        return map;
    }

    private void validateType(String type) {
        if (!BASE_TYPE_NAMES.contains(type)) {
            throw new IllegalArgumentException("Unknown minion type: " + type);
        }
    }
}