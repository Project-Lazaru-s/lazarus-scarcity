package com.lazarus.scarcity;

import com.mojang.serialization.Codec;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.ModList;
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
        if (ModList.get().isLoaded("locks"))
            MinecraftForge.EVENT_BUS.register(new LockChestIntegration());
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
                }))
                .then(Commands.literal("locks")
                        .then(Commands.literal("status").executes(ctx -> {
                            if (!ModList.get().isLoaded("locks")) {
                                ctx.getSource().sendFailure(Component.literal("Locks Reforged não está instalado."));
                                return 0;
                            }
                            var level = ctx.getSource().getLevel();
                            var at = net.minecraft.core.BlockPos.containing(ctx.getSource().getPosition());
                            int cx = at.getX() >> 4, cz = at.getZ() >> 4;
                            int chests = 0, locked = 0, processed = 0, chunks = 0;
                            for (int x = cx - 2; x <= cx + 2; x++)
                                for (int z = cz - 2; z <= cz + 2; z++) {
                                    var chunk = level.getChunkSource().getChunkNow(x, z);
                                    if (chunk == null) continue;
                                    chunks++;
                                    var counts = LockChestIntegration.inspectChunk(level, chunk);
                                    chests += counts.eligible();
                                    locked += counts.locked();
                                    processed += counts.processed();
                                }
                            int found = chests, locks = locked, done = processed, loaded = chunks;
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    "Baús com loot pendente em " + loaded + " chunks carregados: "
                                            + found + "; trancados: " + locks + "; avaliados: " + done), false);
                            return 1;
                        }))
                        .then(Commands.literal("scan").requires(source -> source.hasPermission(2)).executes(ctx -> {
                            if (!ModList.get().isLoaded("locks")) return 0;
                            var level = ctx.getSource().getLevel();
                            var at = net.minecraft.core.BlockPos.containing(ctx.getSource().getPosition());
                            var chunk = level.getChunkSource().getChunkNow(at.getX() >> 4, at.getZ() >> 4);
                            if (chunk == null) return 0;
                            var counts = LockChestIntegration.processChunk(level, chunk);
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                    "Chunk avaliado: " + counts.eligible() + " baús elegíveis, "
                                            + counts.locked() + " novas fechaduras, "
                                            + counts.processed() + " já avaliados."), false);
                            return 1;
                        }))));
    }
}
