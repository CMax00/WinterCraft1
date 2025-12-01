package com.CMax.wintercraft1;

import com.CMax.wintercraft1.command.JobCommand;
import com.CMax.wintercraft1.event.EventHandlers;
import com.CMax.wintercraft1.job.JobManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Wintercraft1 implements ModInitializer {

    public static final String MODID = "wintercraft1";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    @Override
    public void onInitialize() {
        LOGGER.info("WinterCraft1 initialising...");

        // Jobs laden
        JobManager.init();

        // Server stoppt -> Jobs speichern
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            JobManager.save();
            LOGGER.info("Jobs saved on server stopping.");
        });

        // Commands
        JobCommand.register();

        // Events
        EventHandlers.register();

        LOGGER.info("WinterCraft initialised.");
    }
}