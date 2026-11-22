package me.jccer.vanillapermissionsforge.mixin.nbt;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.jccer.vanillapermissionsforge.mixin.accessor.ServerGamePacketListenerImplAccessor;
import me.jccer.vanillapermissionsforge.permission.LpPermissions;
import me.jccer.vanillapermissionsforge.util.Permission;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class NbtQueryMixin {

    @ModifyExpressionValue(
            method = "handleEntityTagQuery",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;hasPermissions(I)Z")
    )
    public boolean addNbtQueryEntity(boolean original) {
        return LpPermissions.checkOr(((ServerGamePacketListenerImplAccessor) this).getPlayer(), Permission.NBT_QUERY_ENTITY, original);
    }

    @ModifyExpressionValue(
            method = "handleBlockEntityTagQuery",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;hasPermissions(I)Z")
    )
    public boolean addNbtQueryBlock(boolean original) {
        return LpPermissions.checkOr(((ServerGamePacketListenerImplAccessor) this).getPlayer(), Permission.NBT_QUERY_BLOCK, original);
    }
}