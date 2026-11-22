package me.jccer.vanillapermissionsforge.mixin.operatorblocks;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.jccer.vanillapermissionsforge.permission.LpPermissions;
import me.jccer.vanillapermissionsforge.util.Permission;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(StructureBlockEntity.class)
public abstract class StructureBlockViewMixin {

    @ModifyExpressionValue(
            method = "usedBy",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;canUseGameMasterBlocks()Z")
    )
    public boolean addStructureBlockView(boolean original, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            return LpPermissions.checkOr(serverPlayer, Permission.OPERATOR_BLOCK_VIEW.formatted(BuiltInRegistries.BLOCK.getKey(Blocks.STRUCTURE_BLOCK).getPath()), original);
        }
        return original;
    }
}