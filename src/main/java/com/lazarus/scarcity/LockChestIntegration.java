package com.lazarus.scarcity;

import melonslise.locks.common.capability.ILockableHandler;
import melonslise.locks.common.init.LocksCapabilities;
import melonslise.locks.common.util.LocksUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/** Covers CityWorld's decorated chunks, where Forge's biome lock feature never runs. */
final class LockChestIntegration {
    private static final String PROCESSED = "lazarusscarcity_lock_processed";
    private static final String REWARD = "lazarusscarcity_locked_reward";
    private static final int CHUNKS_PER_TICK = 8;
    private final Map<ServerLevel, Queue<Long>> pending = new ConcurrentHashMap<>();

    @SubscribeEvent
    public void chunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getChunk() instanceof LevelChunk chunk)) return;
        pending.computeIfAbsent(level, ignored -> new ConcurrentLinkedQueue<>()).add(chunk.getPos().toLong());
    }

    @SubscribeEvent
    public void worldTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) return;
        Queue<Long> queue = pending.get(level);
        if (queue == null) return;
        for (int i = 0; i < CHUNKS_PER_TICK; i++) {
            Long packed = queue.poll();
            if (packed == null) break;
            int x = (int) (packed & 0xffffffffL);
            int z = (int) (packed >>> 32);
            LevelChunk chunk = level.getChunkSource().getChunkNow(x, z);
            if (chunk != null) processChunk(level, chunk);
        }
        // Keep the queue until level unload: a worker may enqueue another chunk concurrently.
    }

    @SubscribeEvent
    public void levelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) pending.remove(level);
    }

    static Counts processChunk(ServerLevel level, LevelChunk chunk) {
        if (!ScarcityConfig.LOCK_GENERATED_CHESTS.get()) return new Counts(0, 0, 0);
        ILockableHandler handler = level.getCapability(LocksCapabilities.LOCKABLE_HANDLER).orElse(null);
        if (handler == null) return new Counts(0, 0, 0);
        int eligible = 0, locked = 0, already = 0;
        for (BlockPos pos : chunk.getBlockEntitiesPos()) {
            BlockEntity entity = chunk.getBlockEntity(pos);
            if (!candidate(level, pos, entity)) continue;
            eligible++;
            if (entity.getPersistentData().getBoolean(PROCESSED)) {
                already++;
                continue;
            }
            if (LocksUtil.intersecting(level, pos).findAny().isPresent()) {
                mark(entity, false);
                already++;
                continue;
            }
            // Uses Locks' configured generation chance and loot-value tiers.
            var lock = LocksUtil.createChestLockable(level, level, pos, level.random);
            if (lock == null) {
                mark(entity, false);
            } else if (handler.add(lock)) {
                mark(entity, true);
                locked++;
            }
            // If add failed (for example, a partner chunk is not ready), retry on its next load.
        }
        return new Counts(eligible, locked, already);
    }

    static boolean candidate(ServerLevel level, BlockPos pos, BlockEntity entity) {
        if (!(entity instanceof RandomizableContainerBlockEntity)) return false;
        if (!level.getBlockState(pos).hasProperty(ChestBlock.TYPE)) return false;
        if (level.getBlockState(pos).getValue(ChestBlock.TYPE) == ChestType.RIGHT) return false;
        // A player-placed or already-opened chest has no pending LootTable tag.
        return entity.saveWithoutMetadata().contains("LootTable", Tag.TAG_STRING);
    }

    static boolean hasReservedReward(ServerLevel level, BlockPos pos) {
        LevelChunk chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        if (chunk == null) return false;
        BlockEntity entity = chunk.getBlockEntity(pos);
        return entity != null && entity.getPersistentData().getBoolean(REWARD);
    }

    private static void mark(BlockEntity entity, boolean reward) {
        entity.getPersistentData().putBoolean(PROCESSED, true);
        if (reward) entity.getPersistentData().putBoolean(REWARD, true);
        entity.setChanged();
    }

    static Counts inspectChunk(ServerLevel level, LevelChunk chunk) {
        int eligible = 0, locked = 0, already = 0;
        for (BlockPos pos : chunk.getBlockEntitiesPos()) {
            BlockEntity entity = chunk.getBlockEntity(pos);
            if (!candidate(level, pos, entity)) continue;
            eligible++;
            if (entity.getPersistentData().getBoolean(PROCESSED)) already++;
            if (LocksUtil.intersecting(level, pos).findAny().isPresent()) locked++;
        }
        return new Counts(eligible, locked, already);
    }

    record Counts(int eligible, int locked, int processed) {}
}
