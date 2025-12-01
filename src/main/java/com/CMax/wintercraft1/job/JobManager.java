package com.CMax.wintercraft1.job;

import com.CMax.wintercraft1.Wintercraft1;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JobManager {
    private static final Map<UUID, Job> JOBS = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = Paths.get("config");
    private static final Path JOBS_FILE = CONFIG_DIR.resolve("jobs.json");
    private static final Type MAP_TYPE = new TypeToken<Map<String, String>>(){}.getType();

    public static synchronized void init() {
        try {
            if (Files.notExists(CONFIG_DIR)) Files.createDirectories(CONFIG_DIR);
            if (Files.exists(JOBS_FILE)) {
                String content = Files.readString(JOBS_FILE, StandardCharsets.UTF_8);
                Map<String, String> map = GSON.fromJson(content, MAP_TYPE);
                if (map != null) {
                    map.forEach((k, v) -> {
                        try {
                            UUID uuid = UUID.fromString(k);
                            Job job = Job.fromString(v);
                            if (job != null) JOBS.put(uuid, job);
                        } catch (Exception ignored) {}
                    });
                }
                Wintercraft1.LOGGER.info("Loaded jobs: " + JOBS.size());
            } else {
                save(); // create empty file
            }
        } catch (IOException e) {
            Wintercraft1.LOGGER.error("Failed to load jobs.json", e);
        }
    }

    public static synchronized void save() {
        try {
            Map<String, String> out = new HashMap<>();
            JOBS.forEach((k, v) -> out.put(k.toString(), v.name()));
            String json = GSON.toJson(out);
            Files.writeString(JOBS_FILE, json, StandardCharsets.UTF_8);
            Wintercraft1.LOGGER.info("Jobs saved (" + out.size() + ")");
        } catch (IOException e) {
            Wintercraft1.LOGGER.error("Failed to save jobs.json", e);
        }
    }

    public static synchronized void setJob(ServerPlayerEntity player, Job job) {
        if (player == null) return;
        if (job == null) job = Job.NONE;
        JOBS.put(player.getUuid(), job);
        save();
    }

    public static synchronized Job getJob(ServerPlayerEntity player) {
        if (player == null) return Job.NONE;
        return JOBS.getOrDefault(player.getUuid(), Job.NONE);
    }

    public static synchronized void setJob(UUID uuid, Job job) {
        if (uuid == null) return;
        if (job == null) job = Job.NONE;
        JOBS.put(uuid, job);
        save();
    }
}