package com.lazarus.scarcity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.common.loot.LootTableIdCondition;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/** A single post-roll rule covers all registered chest tables, including CityWorld's immediate rolls. */
public final class WorldLootModifier extends LootModifier {
    public static final Codec<WorldLootModifier> CODEC = RecordCodecBuilder.create(
            instance -> codecStart(instance).apply(instance, WorldLootModifier::new));

    public WorldLootModifier(LootItemCondition[] conditions) { super(conditions); }

    @Override public Codec<? extends IGlobalLootModifier> codec() { return CODEC; }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        if (loot.isEmpty()) return loot;
        ResourceLocation table = context.getQueriedLootTableId();
        // Some third-party mods call a LootTable object directly; Forge then reports an unknown ID.
        boolean anonymousChest = LootTableIdCondition.UNKNOWN_LOOT_TABLE.equals(table)
                && context.getParamOrNull(LootContextParams.ORIGIN) != null
                && context.getParamOrNull(LootContextParams.THIS_ENTITY) == null
                && context.getParamOrNull(LootContextParams.BLOCK_STATE) == null
                && context.getParamOrNull(LootContextParams.TOOL) == null;
        if (!ScarcityRules.scavengingTable(table) && !anonymousChest) return loot;
        if (context.getParamOrNull(LootContextParams.THIS_ENTITY) != null) return loot;

        int phase = ScarcityRules.phase(ScarcityAge.day(context.getLevel()));
        if (phase == 0) return loot;
        boolean risky = !anonymousChest && ScarcityRules.risky(table);
        if (context.getRandom().nextDouble() >= ScarcityRules.siteChance(phase, risky)) {
            loot.clear();
            return loot;
        }

        List<ItemStack> candidates = new ArrayList<>();
        for (ItemStack stack : loot) {
            if (!stack.isEmpty() && context.getRandom().nextDouble() < ScarcityRules.itemChance(stack, phase))
                candidates.add(stack);
        }
        loot.clear();
        int limit = phase == 1 ? 3 : phase == 2 ? 2 : 1;
        while (!candidates.isEmpty() && loot.size() < limit) {
            ItemStack stack = candidates.remove(context.getRandom().nextInt(candidates.size())).copy();
            // A successful late search yields a handful of items, not a full stack.
            if (phase == 3) stack.setCount(Math.min(stack.getCount(), risky ? 2 : 1));
            else if (phase == 2) stack.setCount(Math.min(stack.getCount(), 3));
            loot.add(stack);
        }
        return loot;
    }
}
