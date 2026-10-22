package com.lazarus.scarcity.mixin;

import com.nerdsquadrados.lootpiles.block.ScrapPileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Once a pile is searched, remove it after LootPiles has completed its normal interaction. */
@Pseudo
@Mixin(ScrapPileBlock.class)
public abstract class ScrapPileBlockMixin {
    @Inject(method = "use", at = @At("RETURN"))
    private void lazarusscarcity$consume(BlockState original, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> callback) {
        if (level.isClientSide || hand != InteractionHand.MAIN_HAND
                || callback.getReturnValue() != InteractionResult.CONSUME) return;
        BlockState after = level.getBlockState(pos);
        if (after.getBlock() == (Block) (Object) this
                && after.getValue(ScrapPileBlock.DEPLETED)) {
            // setBlock never drops the pile item; LootPiles' onRemove clears its cooldown entry.
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }
}
