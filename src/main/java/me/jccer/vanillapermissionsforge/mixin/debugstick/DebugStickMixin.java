package me.jccer.vanillapermissionsforge.mixin.debugstick;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.jccer.vanillapermissionsforge.permission.LpPermissions;
import me.jccer.vanillapermissionsforge.util.Permission;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DebugStickItem;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DebugStickItem.class)
public abstract class DebugStickMixin {

    @ModifyExpressionValue(
            method = "handleInteraction",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;canUseGameMasterBlocks()Z")
    )
    public boolean addDebugStickUse(boolean original, Player player, BlockState state) {
        if (player instanceof ServerPlayer serverPlayer) {
            ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            return LpPermissions.checkOr(serverPlayer, Permission.DEBUG_STICK_USE.formatted(key.getNamespace(), key.getPath()), original);
        }
        return original;
    }
}