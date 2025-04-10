package com.wairesd.dcstackedcases.config;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MaterialsConfig {
    private final ConfigurationNode config;
    private final Map<String, List<String>> loreCache = new ConcurrentHashMap<>();

    public MaterialsConfig(File file) throws IOException {
        this.config = YamlConfigurationLoader.builder().file(file).build().load();
    }

    public String getMaterialString(String caseName) {
        return config.node("Materials", caseName, "ID").getString("CHEST");
    }

    public String getDisplayName(String caseName) {
        return config.node("Materials", caseName, "display_name").getString();
    }

    public List<String> getLore(String caseName, int keyAmount) {
        return loreCache.computeIfAbsent(caseName + "_" + keyAmount, k -> {
            try {
                return config.node("Materials", caseName, "Lore")
                        .getList(String.class, Collections.emptyList())
                        .stream()
                        .map(l -> l.replace("{key}", String.valueOf(keyAmount)))
                        .collect(Collectors.toList());
            } catch (SerializationException e) {
                System.err.println("Error reading lore for case " + caseName + ": " + e.getMessage());
                return Collections.emptyList();
            }
        });
    }
}