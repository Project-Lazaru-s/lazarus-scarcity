package com.lazarus.scarcity.mixin;

import com.lazarus.scarcity.PileScarcity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** CityWorld serves one food item into some kitchen blocks without using a loot table. */
@Pseudo
@Mixin(targets = "me.daddychurchill.CityWorld.Support.ContainerLoot", remap = false)
public abstract class CityWorldKitchenMixin {
    @Inject(method = "serveFood", at = @At("HEAD"), cancellable = true, remap = false)
    private static void lazarusscarcity$kitchenFood(WorldGenLevel level, BlockPos pos, BlockEntity entity,
            BlockState state, long seed, CallbackInfoReturnable<Boolean> callback) {
        if (!PileScarcity.allow(level.getLevel(), pos, "common")) callback.setReturnValue(false);
    }
}
