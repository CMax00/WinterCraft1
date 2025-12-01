package com.CMax.wintercraft1.command;

import com.CMax.wintercraft1.Wintercraft1;
import com.CMax.wintercraft1.job.Job;
import com.CMax.wintercraft1.job.JobManager;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.Set;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class JobCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // /jobw add <job> <player>
            dispatcher.register(literal("jobw")
                    .then(literal("add")
                            .then(argument("job", StringArgumentType.word())
                                    .then(argument("player", GameProfileArgumentType.gameProfile())
                                            .executes(context -> {
                                                // Prüfen, ob der Befehl von einem Spieler kommt
                                                if (context.getSource().getPlayer() != null) {
                                                    context.getSource().sendFeedback(() -> Text.literal("Dieser Befehl kann nur von der Konsole ausgeführt werden."), false);
                                                    return 0;
                                                }

                                                String jobName = StringArgumentType.getString(context, "job");
                                                Collection<GameProfile> profiles = GameProfileArgumentType.getProfileArgument(context, "player");
                                                Job job = Job.fromString(jobName);

                                                if (job == null || job == Job.NONE) {
                                                    context.getSource().sendFeedback(() -> Text.literal("Ungültiger Job: " + jobName), false);
                                                    return 0;
                                                }

                                                for (GameProfile profile : profiles) {
                                                    ServerPlayerEntity player = context.getSource().getServer().getPlayerManager().getPlayer(profile.getId());
                                                    if (player != null) {
                                                        if (JobManager.hasJob(player, job)) {
                                                            player.sendMessage(Text.literal("§cDu hast bereits den Job '" + job.name() + "'."));
                                                            context.getSource().sendFeedback(() -> Text.literal("§c" + profile.getName() + " hat bereits Job '" + job.name() + "'."), false);
                                                        } else {
                                                            JobManager.addJob(player, job);
                                                            player.sendMessage(Text.literal("§aJob '" + job.name() + "' wurde hinzugefügt."));
                                                            context.getSource().sendFeedback(() -> Text.literal("§aJob '" + job.name() + "' für " + profile.getName() + " hinzugefügt."), false);
                                                        }
                                                    } else {
                                                        context.getSource().sendFeedback(() -> Text.literal("§cSpieler nicht online: " + profile.getName()), false);
                                                    }
                                                }

                                                return 1;
                                            })
                                    )
                            )
                    )
            );

            // /jobw remove <job> <player>
            dispatcher.register(literal("jobw")
                    .then(literal("remove")
                            .then(argument("job", StringArgumentType.word())
                                    .then(argument("player", GameProfileArgumentType.gameProfile())
                                            .executes(context -> {
                                                // Prüfen, ob der Befehl von einem Spieler kommt
                                                if (context.getSource().getPlayer() != null) {
                                                    context.getSource().sendFeedback(() -> Text.literal("Dieser Befehl kann nur von der Konsole ausgeführt werden."), false);
                                                    return 0;
                                                }

                                                String jobName = StringArgumentType.getString(context, "job");
                                                Collection<GameProfile> profiles = GameProfileArgumentType.getProfileArgument(context, "player");
                                                Job job = Job.fromString(jobName);

                                                if (job == null) {
                                                    context.getSource().sendFeedback(() -> Text.literal("Ungültiger Job: " + jobName), false);
                                                    return 0;
                                                }

                                                for (GameProfile profile : profiles) {
                                                    ServerPlayerEntity player = context.getSource().getServer().getPlayerManager().getPlayer(profile.getId());
                                                    if (player != null) {
                                                        boolean removed = JobManager.removeJob(player, job);
                                                        if (removed) {
                                                            player.sendMessage(Text.literal("§eJob '" + job.name() + "' wurde entfernt."));
                                                            context.getSource().sendFeedback(() -> Text.literal("§eJob '" + job.name() + "' von " + profile.getName() + " entfernt."), false);
                                                        } else {
                                                            player.sendMessage(Text.literal("§cDu hattest Job '" + job.name() + "' nicht."));
                                                            context.getSource().sendFeedback(() -> Text.literal("§c" + profile.getName() + " hatte Job '" + job.name() + "' nicht."), false);
                                                        }
                                                    } else {
                                                        context.getSource().sendFeedback(() -> Text.literal("§cSpieler nicht online: " + profile.getName()), false);
                                                    }
                                                }

                                                return 1;
                                            })
                                    )
                            )
                    )
            );

            // /jobw list <player>
            dispatcher.register(literal("jobw")
                    .then(literal("list")
                            .then(argument("player", GameProfileArgumentType.gameProfile())
                                    .executes(context -> {
                                        Collection<GameProfile> profiles = GameProfileArgumentType.getProfileArgument(context, "player");

                                        for (GameProfile profile : profiles) {
                                            ServerPlayerEntity player = context.getSource().getServer().getPlayerManager().getPlayer(profile.getId());
                                            if (player != null) {
                                                Set<Job> jobs = JobManager.getJobs(player);
                                                if (jobs.isEmpty()) {
                                                    context.getSource().sendFeedback(() -> Text.literal("§7" + profile.getName() + " hat keine Jobs."), false);
                                                } else {
                                                    StringBuilder jobList = new StringBuilder();
                                                    boolean first = true;
                                                    for (Job job : jobs) {
                                                        if (!first) jobList.append("§7, §a");
                                                        else jobList.append("§a");
                                                        jobList.append(job.name());
                                                        first = false;
                                                    }
                                                    String finalJobList = jobList.toString();
                                                    context.getSource().sendFeedback(() -> Text.literal("§6" + profile.getName() + "§7's Jobs: " + finalJobList), false);
                                                }
                                            } else {
                                                context.getSource().sendFeedback(() -> Text.literal("§cSpieler nicht online: " + profile.getName()), false);
                                            }
                                        }

                                        return 1;
                                    })
                            )
                    )
            );
        });

        Wintercraft1.LOGGER.info("JobCommand registered");
    }
}