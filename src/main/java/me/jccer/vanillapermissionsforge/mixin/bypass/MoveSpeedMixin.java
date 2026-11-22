package me.jccer.vanillapermissionsforge.mixin.bypass;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.jccer.vanillapermissionsforge.mixin.accessor.ServerGamePacketListenerImplAccessor;
import me.jccer.vanillapermissionsforge.permission.LpPermissions;
import me.jccer.vanillapermissionsforge.util.Permission;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MoveSpeedMixin {

    @ModifyExpressionValue(
            method = "handleMovePlayer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;isSingleplayerOwner()Z")
    )
    public boolean addMoveSpeedPlayerBypass(boolean original) {
        ServerGamePacketListenerImplAccessor self = (ServerGamePacketListenerImplAccessor) this;
        return LpPermissions.checkOr(self.getPlayer(), Permission.BYPASS_MOVE_SPEED_PLAYER, original);
    }

    @ModifyExpressionValue(
            method = "handleMoveVehicle",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;isSingleplayerOwner()Z")
    )
    public boolean addMoveSpeedVehicleBypass(boolean original) {
        ServerPlayer player = ((ServerGamePacketListenerImplAccessor) this).getPlayer();
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(player.getRootVehicle().getType());
        return LpPermissions.checkOr(player, Permission.BYPASS_MOVE_SPEED_VEHICLE.formatted(key.getNamespace(), key.getPath()), original);
    }
}