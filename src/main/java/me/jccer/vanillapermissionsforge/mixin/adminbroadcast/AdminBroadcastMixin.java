package me.jccer.vanillapermissionsforge.mixin.adminbroadcast;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.jccer.vanillapermissionsforge.permission.LpPermissions;
import me.jccer.vanillapermissionsforge.util.Permission;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CommandSourceStack.class)
public abstract class AdminBroadcastMixin {

    @ModifyExpressionValue(
            method = "broadcastToAdmins",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;isOp(Lcom/mojang/authlib/GameProfile;)Z")
    )
    public boolean addAdminBroadcastReceive(boolean original, @Local ServerPlayer player) {
        return LpPermissions.checkOr(player, Permission.ADMIN_BROADCAST_RECEIVE, original);
    }
}