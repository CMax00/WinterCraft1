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
import java.util.*;

public class JobManager {
    private static final Map<UUID, Set<Job>> JOBS = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = Paths.get("config");
    private static final Path JOBS_FILE = CONFIG_DIR.resolve("jobs.json");
    private static final Type MAP_TYPE = new TypeToken<Map<String, List<String>>>(){}.getType();

    public static synchronized void init() {
        try {
            if (Files.notExists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
                Wintercraft1.LOGGER.info("Created config directory");
            }

            if (Files.exists(JOBS_FILE)) {
                String content = Files.readString(JOBS_FILE, StandardCharsets.UTF_8);
                if (content.trim().isEmpty()) {
                    Wintercraft1.LOGGER.warn("jobs.json is empty, initializing with empty data");
                    save();
                    return;
                }

                Map<String, List<String>> map = GSON.fromJson(content, MAP_TYPE);
                if (map != null) {
                    map.forEach((k, v) -> {
                        try {
                            UUID uuid = UUID.fromString(k);
                            Set<Job> jobs = new HashSet<>();
                            if (v != null) {
                                for (String jobName : v) {
                                    Job job = Job.fromString(jobName);
                                    if (job != null && job != Job.NONE) {
                                        jobs.add(job);
                                    }
                                }
                            }
                            if (!jobs.isEmpty()) {
                                JOBS.put(uuid, jobs);
                            }
                        } catch (Exception e) {
                            Wintercraft1.LOGGER.warn("Failed to parse job entry for " + k + ": " + e.getMessage());
                        }
                    });
                }
                Wintercraft1.LOGGER.info("Loaded jobs for " + JOBS.size() + " players");
            } else {
                Wintercraft1.LOGGER.info("jobs.json not found, creating new file");
                save();
            }
        } catch (IOException e) {
            Wintercraft1.LOGGER.error("Failed to load jobs.json", e);
        }
    }

    public static synchronized void save() {
        try {
            Map<String, List<String>> out = new HashMap<>();
            JOBS.forEach((uuid, jobs) -> {
                List<String> jobNames = new ArrayList<>();
                jobs.forEach(job -> jobNames.add(job.name()));
                out.put(uuid.toString(), jobNames);
            });
            String json = GSON.toJson(out);
            Files.writeString(JOBS_FILE, json, StandardCharsets.UTF_8);
            Wintercraft1.LOGGER.info("Jobs saved (" + JOBS.size() + " players)");
        } catch (IOException e) {
            Wintercraft1.LOGGER.error("Failed to save jobs.json", e);
        }
    }

    public static synchronized void addJob(ServerPlayerEntity player, Job job) {
        if (player == null || job == null || job == Job.NONE) return;
        JOBS.computeIfAbsent(player.getUuid(), k -> new HashSet<>()).add(job);
        save();
    }

    public static synchronized void addJob(UUID uuid, Job job) {
        if (uuid == null || job == null || job == Job.NONE) return;
        JOBS.computeIfAbsent(uuid, k -> new HashSet<>()).add(job);
        save();
    }

    public static synchronized boolean removeJob(ServerPlayerEntity player, Job job) {
        if (player == null || job == null) return false;
        Set<Job> jobs = JOBS.get(player.getUuid());
        if (jobs != null) {
            boolean removed = jobs.remove(job);
            if (jobs.isEmpty()) {
                JOBS.remove(player.getUuid());
            }
            if (removed) save();
            return removed;
        }
        return false;
    }

    public static synchronized boolean removeJob(UUID uuid, Job job) {
        if (uuid == null || job == null) return false;
        Set<Job> jobs = JOBS.get(uuid);
        if (jobs != null) {
            boolean removed = jobs.remove(job);
            if (jobs.isEmpty()) {
                JOBS.remove(uuid);
            }
            if (removed) save();
            return removed;
        }
        return false;
    }

    public static synchronized Set<Job> getJobs(ServerPlayerEntity player) {
        if (player == null) return Collections.emptySet();
        return new HashSet<>(JOBS.getOrDefault(player.getUuid(), Collections.emptySet()));
    }

    public static synchronized Set<Job> getJobs(UUID uuid) {
        if (uuid == null) return Collections.emptySet();
        return new HashSet<>(JOBS.getOrDefault(uuid, Collections.emptySet()));
    }

    public static synchronized boolean hasJob(ServerPlayerEntity player, Job job) {
        if (player == null || job == null) return false;
        Set<Job> jobs = JOBS.get(player.getUuid());
        return jobs != null && jobs.contains(job);
    }
}