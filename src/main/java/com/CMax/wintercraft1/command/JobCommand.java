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

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class JobCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("jobw")
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
                                                JobManager.setJob(player, job);
                                                player.sendMessage(Text.literal("Dein Job wurde auf '" + job.name() + "' gesetzt."));
                                                context.getSource().sendFeedback(() -> Text.literal("Setze Job '" + job.name() + "' für " + profile.getName()), false);
                                            } else {
                                                context.getSource().sendFeedback(() -> Text.literal("Spieler nicht online: " + profile.getName()), false);
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
