package com.CMax.wintercraft1.event;

import com.CMax.wintercraft1.job.Job;
import com.CMax.wintercraft1.job.JobManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.text.Text;

public class EventHandlers {
    public static void register() {
        registerUseBlock();
        registerBlockBreak();
        registerPlayerTick();
    }

    private static void registerUseBlock() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!(player instanceof ServerPlayerEntity sp)) return ActionResult.PASS;
            Block block = world.getBlockState(hitResult.getBlockPos()).getBlock();

            // Farmer -> Smoker
            if (block == Blocks.SMOKER && !JobManager.hasJob(sp, Job.FARMER)) {
                sp.sendMessage(Text.literal("Nur der Farmer darf Räucheröfen benutzen!"));
                return ActionResult.FAIL;
            }

            // Schmied -> Smithing Table & Blast Furnace
            if ((block == Blocks.SMITHING_TABLE || block == Blocks.BLAST_FURNACE) && !JobManager.hasJob(sp, Job.SCHMIED)) {
                sp.sendMessage(Text.literal("Nur der Schmied darf diesen Block benutzen!"));
                return ActionResult.FAIL;
            }

            // Alchemist -> Brewing Stand
            if (block == Blocks.BREWING_STAND && !JobManager.hasJob(sp, Job.ALCHEMIST)) {
                sp.sendMessage(Text.literal("Nur der Alchemist darf Braustände benutzen!"));
                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });
    }

    private static void registerBlockBreak() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (!(player instanceof ServerPlayerEntity sp)) return true;
            Block block = state.getBlock();

            // Miner -> Ancient Debris
            if (block == Blocks.ANCIENT_DEBRIS && !JobManager.hasJob(sp, Job.MINER)) {
                sp.sendMessage(Text.literal("Nur ein Minenarbeiter kann Ancient Debris abbauen!"));
                return false;
            }

            return true;
        });
    }

    private static void registerPlayerTick() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // Für jeden Spieler die Effekte anwenden
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                var item = player.getMainHandStack();

                // Farmer Effekt
                if (JobManager.hasJob(player, Job.FARMER)) {
                    if (item.getItem() instanceof net.minecraft.item.ShovelItem) {
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 42, 1, true, false));
                    }
                }

                // Fischer Effekt
                if (!JobManager.hasJob(player, Job.FISCHER)) {
                    if (item.getItem() instanceof net.minecraft.item.FishingRodItem) {
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.UNLUCK, 200, 9, true, false));
                    }
                }

                // Explorer Effekt
                if (JobManager.hasJob(player, Job.EXPLORER)) {
                    if (player.getEquippedStack(EquipmentSlot.FEET).getItem() == Items.LEATHER_BOOTS) {
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 220, 3, true, false));
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 220, 3, true, false));
                    }
                }
            }
        });
    }
}