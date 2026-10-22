package com.lazarus.scarcity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** The highest overworld day reached. Sleeping advances the day; setting time backwards cannot restore loot. */
final class ScarcityAge extends SavedData {
    private static final String KEY = "lazarusscarcity_age";
    private long maximumDay;

    static ScarcityAge load(CompoundTag tag) {
        ScarcityAge result = new ScarcityAge();
        result.maximumDay = Math.max(0, tag.getLong("MaximumDay"));
        return result;
    }

    static int day(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        ScarcityAge data = overworld.getDataStorage().computeIfAbsent(ScarcityAge::load, ScarcityAge::new, KEY);
        long current = Math.max(0, overworld.getDayTime() / 24000L);
        if (current > data.maximumDay) {
            data.maximumDay = current;
            data.setDirty();
        }
        return (int) Math.min(Integer.MAX_VALUE, data.maximumDay);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLong("MaximumDay", maximumDay);
        return tag;
    }
}
