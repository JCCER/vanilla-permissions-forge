package me.jccer.vanillapermissionsforge.mixin.bypass;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import me.jccer.vanillapermissionsforge.util.LoginCache;
import me.jccer.vanillapermissionsforge.util.Permission;
import net.luckperms.api.util.Tristate;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DedicatedPlayerList.class)
public abstract class PlayerLimitMixin {

    @ModifyExpressionValue(
            method = "canBypassPlayerLimit",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/ServerOpList;canBypassPlayerLimit(Lcom/mojang/authlib/GameProfile;)Z")
    )
    public boolean addPlayerLimitBypass(boolean original, GameProfile profile) {
        Tristate state = LoginCache.get(profile.getId(), Permission.BYPASS_PLAYER_LIMIT);
        return state == Tristate.UNDEFINED ? original : state == Tristate.TRUE;
    }
}