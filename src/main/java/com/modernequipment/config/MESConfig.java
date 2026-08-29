package com.modernequipment.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = com.modernequipment.MESMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MESConfig {

    public static final ForgeConfigSpec CLIENT_CONFIG;
    public static final ForgeConfigSpec COMMON_CONFIG;
    private static final Client CLIENT;
    private static final Common COMMON;

    static {
        final var client = new Client();
        CLIENT_CONFIG = new ForgeConfigSpec.Builder().configure(builder -> {
            client.build(builder);
            return client;
        }).getValue();
        CLIENT = client;

        final var common = new Common();
        COMMON_CONFIG = new ForgeConfigSpec.Builder().configure(builder -> {
            common.build(builder);
            return common;
        }).getValue();
        COMMON = common;
    }

    public static boolean isDebugLoggingEnabled() {
        return CLIENT.debugLogging.get();
    }

    public static int getBuiltinRigColumns() { return COMMON.rigColumns.get(); }
    public static int getBuiltinRigRows() { return COMMON.rigRows.get(); }
    public static int getBuiltinBackpackColumns() { return COMMON.backpackColumns.get(); }
    public static int getBuiltinBackpackRows() { return COMMON.backpackRows.get(); }
    public static int getBuiltinSafeBoxColumns() { return COMMON.safeBoxColumns.get(); }
    public static int getBuiltinSafeBoxRows() { return COMMON.safeBoxRows.get(); }

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

    public static class Common {
        private ForgeConfigSpec.IntValue rigColumns;
        private ForgeConfigSpec.IntValue rigRows;
        private ForgeConfigSpec.IntValue backpackColumns;
        private ForgeConfigSpec.IntValue backpackRows;
        private ForgeConfigSpec.IntValue safeBoxColumns;
        private ForgeConfigSpec.IntValue safeBoxRows;

        void build(ForgeConfigSpec.Builder builder) {
            builder.comment("Default storage of the built-in MES Curios items")
                    .push("builtin_storage");
            rigColumns = builder.defineInRange("chest_rig_columns", 5, 1, 16);
            rigRows = builder.defineInRange("chest_rig_rows", 4, 1, 16);
            backpackColumns = builder.defineInRange("backpack_columns", 8, 1, 16);
            backpackRows = builder.defineInRange("backpack_rows", 6, 1, 16);
            safeBoxColumns = builder.defineInRange("safe_box_columns", 3, 1, 16);
            safeBoxRows = builder.defineInRange("safe_box_rows", 3, 1, 16);
            builder.pop();
        }
    }
}
