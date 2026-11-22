package me.jccer.vanillapermissionsforge.mixin.operatorblocks;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.jccer.vanillapermissionsforge.mixin.accessor.ServerPlayerGameModeAccessor;
import me.jccer.vanillapermissionsforge.permission.LpPermissions;
import me.jccer.vanillapermissionsforge.util.Permission;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayerGameMode.class)
public abstract class OperatorBlockBreakMixin {

    @ModifyExpressionValue(
            method = "destroyBlock",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;canUseGameMasterBlocks()Z")
    )
    public boolean addOperatorBlockBreak(boolean original, BlockPos pos) {
        ServerPlayerGameModeAccessor self = (ServerPlayerGameModeAccessor) this;
        Block block = self.getLevel().getBlockState(pos).getBlock();
        return LpPermissions.checkOr(self.getPlayer(), Permission.OPERATOR_BLOCK_BREAK.formatted(BuiltInRegistries.BLOCK.getKey(block).getPath()), original);
    }
}