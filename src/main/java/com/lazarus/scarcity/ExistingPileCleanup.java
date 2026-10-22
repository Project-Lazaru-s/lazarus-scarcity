package com.lazarus.scarcity;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.WeakHashMap;

/** Migrate already-depleted LootPiles without scanning every block in loaded chunks. */
final class ExistingPileCleanup {
    private static final Map<ServerLevel, ArrayDeque<BlockPos>> PENDING = new WeakHashMap<>();
    private static final int MAX_PER_TICK = 64;

    @SubscribeEvent
    public void chunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getChunk() instanceof LevelChunk chunk)) return;
        for (BlockPos pos : chunk.getBlockEntities().keySet()) {
            if (depletedPile(chunk.getBlockState(pos)))
                PENDING.computeIfAbsent(level, ignored -> new ArrayDeque<>()).add(pos.immutable());
        }
    }

    @SubscribeEvent
    public void worldTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) return;
        ArrayDeque<BlockPos> queue = PENDING.get(level);
        if (queue == null) return;
        for (int i = 0; i < MAX_PER_TICK && !queue.isEmpty(); i++) {
            BlockPos pos = queue.removeFirst();
            if (level.isLoaded(pos) && depletedPile(level.getBlockState(pos)))
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
        if (queue.isEmpty()) PENDING.remove(level);
    }

    static boolean depletedPile(BlockState state) {
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (id == null || !"lootpiles".equals(id.getNamespace())) return false;
        for (var property : state.getProperties()) {
            if (property instanceof BooleanProperty booleanProperty && "depleted".equals(property.getName()))
                return state.getValue(booleanProperty);
        }
        return false;
    }
}
