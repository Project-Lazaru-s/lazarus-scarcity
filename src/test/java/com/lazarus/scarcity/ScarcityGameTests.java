package com.lazarus.scarcity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.common.util.FakePlayerFactory;
import com.nerdsquadrados.lootpiles.loot.ScrapLootService;
import com.nerdsquadrados.lootpiles.block.ScrapPileBlock;
import com.nerdsquadrados.lootpiles.registry.ScrapPileRegistry;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@GameTestHolder(LazarusScarcity.ID)
@PrefixGameTestTemplate(false)
public final class ScarcityGameTests {
    @GameTest(template = "scarcity_arena", timeoutTicks = 100)
    public static void dayThirtyMakesCommonStashesRare(GameTestHelper h) throws Exception {
        var level = h.getLevel();
        var table = level.getServer().getLootData().getLootTable(
                new ResourceLocation(LazarusScarcity.ID, "chests/test_supply"));
        var params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(h.absolutePos(BlockPos.ZERO)))
                .create(LootContextParamSets.CHEST);
        h.assertTrue(ScarcityRules.phase(0) == 0, "fresh worlds use the unmodified table");
        ScarcityConfig.COMMON[3].set(0.16);
        level.getServer().overworld().setDayTime(30L * 24000L);
        int filled = 0;
        for (int i = 0; i < 1000; i++) if (!table.getRandomItems(params).isEmpty()) filled++;
        h.assertTrue(filled >= 70 && filled <= 160,
                "day 30 should fill about one in ten ordinary stashes; got " + filled);
        level.getServer().overworld().setDayTime(0);
        h.assertTrue(ScarcityAge.day(level) >= 30, "turning time back cannot restore supplies");
        BlockPos pos = h.absolutePos(new BlockPos(5, 2, 5));
        ScarcityConfig.COMMON[3].set(1.0);
        ScrapLootService.spawnLoot(level, pos, ScrapPileRegistry.getDefinition("common"), null);
        var area = new AABB(pos).inflate(2);
        var first = level.getEntitiesOfClass(ItemEntity.class, area);
        h.assertTrue(!first.isEmpty(), "a permitted common pile should spawn its normal loot");
        first.forEach(ItemEntity::discard);
        ScarcityConfig.COMMON[3].set(0.0);
        ScrapLootService.spawnLoot(level, pos, ScrapPileRegistry.getDefinition("common"), null);
        h.assertTrue(level.getEntitiesOfClass(ItemEntity.class, area).isEmpty(),
                "a depleted common pile must not spawn items");
        var pileBlock = ScrapPileRegistry.byId("common").orElseThrow().block().get();
        level.setBlock(pos, pileBlock.defaultBlockState(), 3);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        var fakePlayer = FakePlayerFactory.getMinecraft(level);
        pileBlock.use(level.getBlockState(pos), level, pos, fakePlayer, InteractionHand.MAIN_HAND, hit);
        h.assertTrue(level.getBlockState(pos).isAir(), "searched empty pile must disappear permanently");
        ScarcityConfig.COMMON[3].set(1.0);
        level.setBlock(pos, pileBlock.defaultBlockState(), 3);
        pileBlock.use(level.getBlockState(pos), level, pos, fakePlayer, InteractionHand.MAIN_HAND, hit);
        h.assertTrue(level.getBlockState(pos).isAir(), "searched full pile must disappear permanently");
        level.setBlock(pos, pileBlock.defaultBlockState().setValue(ScrapPileBlock.DEPLETED, true), 3);
        var cleanup = new ExistingPileCleanup();
        cleanup.chunkLoad(new ChunkEvent.Load(level.getChunkAt(pos), false));
        cleanup.worldTick(new TickEvent.LevelTickEvent(LogicalSide.SERVER, TickEvent.Phase.END, level, () -> true));
        h.assertTrue(level.getBlockState(pos).isAir(), "an old depleted pile must disappear on chunk load");
        level.getEntitiesOfClass(ItemEntity.class, area).forEach(ItemEntity::discard);
        ScarcityConfig.COMMON[3].set(0.0);
        var kitchen = Class.forName("me.daddychurchill.CityWorld.Support.ContainerLoot")
                .getDeclaredMethod("serveFood", WorldGenLevel.class, BlockPos.class,
                        BlockEntity.class, BlockState.class, long.class);
        kitchen.setAccessible(true);
        for (int i = 0; i < 20; i++) {
            BlockState chest = Blocks.CHEST.defaultBlockState();
            var empty = new ChestBlockEntity(pos, chest);
            h.assertTrue(!(Boolean) kitchen.invoke(null, level, pos, empty, chest, (long) i),
                    "a depleted CityWorld kitchen must not receive fresh food");
        }
        ScarcityConfig.COMMON[3].set(0.16);
        h.succeed();
    }
}
