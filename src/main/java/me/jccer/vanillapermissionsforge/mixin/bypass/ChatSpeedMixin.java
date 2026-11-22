package me.jccer.vanillapermissionsforge.mixin.bypass;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.jccer.vanillapermissionsforge.mixin.accessor.ServerGamePacketListenerImplAccessor;
import me.jccer.vanillapermissionsforge.permission.LpPermissions;
import me.jccer.vanillapermissionsforge.util.Permission;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ChatSpeedMixin {

    @ModifyExpressionValue(
            method = "detectRateSpam",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;isOp(Lcom/mojang/authlib/GameProfile;)Z")
    )
    public boolean addChatSpeedBypass(boolean original) {
        ServerGamePacketListenerImplAccessor self = (ServerGamePacketListenerImplAccessor) this;
        return LpPermissions.checkOr(self.getPlayer(), Permission.BYPASS_CHAT_SPEED, original);
    }
}