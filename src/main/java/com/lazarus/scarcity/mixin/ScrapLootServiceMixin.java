package com.lazarus.scarcity.mixin;

import com.lazarus.scarcity.PileScarcity;
import com.nerdsquadrados.lootpiles.registry.ScrapPileDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.nerdsquadrados.lootpiles.loot.ScrapLootService", remap = false)
public abstract class ScrapLootServiceMixin {
    @Inject(method = "spawnLoot", at = @At("HEAD"), cancellable = true, remap = false)
    private static void lazarusscarcity$emptyPile(ServerLevel level, BlockPos pos,
            ScrapPileDefinition pile, Player player, CallbackInfo callback) {
        if (!PileScarcity.allow(level, pos, pile.id())) callback.cancel();
    }
}
