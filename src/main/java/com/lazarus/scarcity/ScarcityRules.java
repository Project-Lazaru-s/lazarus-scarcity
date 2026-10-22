package com.lazarus.scarcity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

/** Pure policy shared by chest tables and custom LootPiles. */
public final class ScarcityRules {
    private ScarcityRules() {}

    public static int phase(int day) {
        if (day >= ScarcityConfig.FOURTH_DAY.get()) return 3;
        if (day >= ScarcityConfig.THIRD_DAY.get()) return 2;
        if (day >= ScarcityConfig.SECOND_DAY.get()) return 1;
        return 0;
    }

    public static double siteChance(int phase, boolean risky) {
        return (risky ? ScarcityConfig.RISKY : ScarcityConfig.COMMON)[phase].get();
    }

    public static boolean risky(ResourceLocation id) {
        String s = id.getPath().toLowerCase(Locale.ROOT);
        return s.contains("vault") || s.contains("military") || s.contains("armour")
                || s.contains("weapon") || s.contains("airdrop") || s.contains("rare")
                || s.contains("legendary") || s.contains("hospital") || s.contains("medic")
                || s.contains("police") || s.contains("cops") || s.contains("ancient_city")
                || s.contains("bastion") || s.contains("end_city") || s.contains("stronghold")
                || s.contains("treasure") || s.contains("temple") || s.contains("pyramid")
                || s.contains("outpost") || s.contains("mansion") || s.contains("nether_bridge");
    }

    /** Only exploration containers. Combat, block breaking, farming, fishing and quest payouts are outside scope. */
    public static boolean scavengingTable(ResourceLocation id) {
        String path = id.getPath();
        return path.startsWith("chests/") || path.startsWith("containers/");
    }

    public static double itemChance(ItemStack stack, int phase) {
        if (phase == 0) return 1;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String s = id.getPath().toLowerCase(Locale.ROOT);
        double late;
        if (stack.isEdible() || s.contains("food") || s.contains("drink") || s.contains("water")) late = 0.68;
        else if (s.contains("bandage") || s.contains("medicine") || s.contains("antibiotic")
                || s.contains("syringe") || s.contains("medkit")) late = 0.90;
        else if (s.contains("ammo") || s.contains("bullet") || s.contains("magazine")
                || s.contains("cartridge")) late = 0.74;
        else if (s.contains("helmet") || s.contains("chestplate") || s.contains("gun")
                || s.contains("weapon") || s.contains("boots")) late = 0.82;
        else late = 0.80;
        return 1 - (1 - late) * phase / 3.0;
    }
}
