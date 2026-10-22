package com.lazarus.scarcity;

import com.mojang.serialization.Codec;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.common.loot.IGlobalLootModifier;

@Mod(LazarusScarcity.ID)
public final class LazarusScarcity {
    public static final String ID = "lazarusscarcity";
    private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ID);
    static final RegistryObject<Codec<? extends IGlobalLootModifier>> WORLD_LOOT =
            MODIFIERS.register("world_scavenging", () -> WorldLootModifier.CODEC);

    public LazarusScarcity() {
        MODIFIERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ScarcityConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ExistingPileCleanup());
    }

    @SubscribeEvent
    public void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("lazarusscarcity")
                .then(Commands.literal("status").executes(ctx -> {
                    var level = ctx.getSource().getLevel();
                    int day = ScarcityAge.day(level);
                    int phase = ScarcityRules.phase(day);
                    ctx.getSource().sendSuccess(() -> Component.literal("Lazarus Scarcity: dia " + day
                            + ", fase " + phase + ", chance comum "
                            + Math.round(ScarcityRules.siteChance(phase, false) * 100) + "%"
                            + ", local arriscado "
                            + Math.round(ScarcityRules.siteChance(phase, true) * 100) + "%"), false);
                    return 1;
                })));
    }
}
