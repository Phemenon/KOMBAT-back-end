package com.kombat.backend.game;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DefaultProvider {

    private static final List<String> BASE_TYPE_NAMES = List.of(
            "Factory",
            "Calmer",
            "Berserker",
            "Bomber",
            "Gambler"
    );

    private DefaultProvider() {
    }

    public static Map<String, Long> defaultDefendFactorMap() {
        Map<String, Long> map = new LinkedHashMap<>();

        for (String type : BASE_TYPE_NAMES) {
            map.put(type, 0L);
        }

        return map;
    }

    public static Map<String, String> defaultStrategySourceMap() throws IOException {
        Map<String, String> map = new LinkedHashMap<>();

        for (String type : BASE_TYPE_NAMES) {
            String path = "strategyFile/" + type + ".txt";

            ClassPathResource resource = new ClassPathResource(path);

            if (!resource.exists()) {
                throw new IllegalStateException("Strategy file not found: " + path);
            }

            try (InputStream is = resource.getInputStream()) {
                String source = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                map.put(type, source);
            }
        }

        return map;
    }
}