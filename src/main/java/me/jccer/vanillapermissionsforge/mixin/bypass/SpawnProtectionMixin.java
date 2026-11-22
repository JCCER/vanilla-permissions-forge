package me.jccer.vanillapermissionsforge.mixin.bypass;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.jccer.vanillapermissionsforge.permission.LpPermissions;
import me.jccer.vanillapermissionsforge.util.Permission;
import net.minecraft.core.BlockPos;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DedicatedServer.class)
public abstract class SpawnProtectionMixin {

    @ModifyExpressionValue(
            method = "isUnderSpawnProtection",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/DedicatedPlayerList;isOp(Lcom/mojang/authlib/GameProfile;)Z")
    )
    public boolean addSpawnProtectionBypass(boolean original, ServerLevel level, BlockPos pos, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            return LpPermissions.checkOr(serverPlayer, Permission.BYPASS_SPAWN_PROTECTION, original);
        }
        return original;
    }
}