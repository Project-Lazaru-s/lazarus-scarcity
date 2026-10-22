package com.lazarus.scarcity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/** LootPiles rolls outside Minecraft's loot table pipeline, so it needs an entry-point adapter. */
public final class PileScarcity {
    private PileScarcity() {}

    public static boolean allow(ServerLevel level, BlockPos pos, String pileId) {
        int phase = ScarcityRules.phase(ScarcityAge.day(level));
        if (phase == 0) return true;
        boolean risky = pileId.equals("rare") || pileId.equals("legendary")
                || pileId.equals("medic") || pileId.equals("soldier");
        return level.random.nextDouble() < ScarcityRules.siteChance(phase, risky);
    }
}
