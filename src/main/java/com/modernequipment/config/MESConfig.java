package com.modernequipment.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = com.modernequipment.MESMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MESConfig {

    public static final ForgeConfigSpec CLIENT_CONFIG;
    private static final Client CLIENT;

    static {
        final var client = new Client();
        CLIENT_CONFIG = new ForgeConfigSpec.Builder().configure(builder -> {
            client.build(builder);
            return client;
        }).getValue();
        CLIENT = client;
    }

    public static boolean isDebugLoggingEnabled() {
        return CLIENT.debugLogging.get();
    }

    public static class Client {
        public ForgeConfigSpec.BooleanValue debugLogging;

        void build(ForgeConfigSpec.Builder builder) {
            builder.comment("Client-side debug logging settings")
                    .push("debug");

            debugLogging = builder
                    .comment("Enable debug logging for GeoCurioRenderer and DynamicCurioModel")
                    .define("debug_logging", false);

            builder.pop();
        }
    }
}