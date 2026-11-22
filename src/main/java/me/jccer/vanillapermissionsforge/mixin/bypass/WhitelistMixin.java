package me.jccer.vanillapermissionsforge.mixin.bypass;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import me.jccer.vanillapermissionsforge.util.LoginCache;
import me.jccer.vanillapermissionsforge.util.Permission;
import net.luckperms.api.util.Tristate;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DedicatedPlayerList.class)
public abstract class WhitelistMixin {

    @ModifyReturnValue(method = "isWhiteListed", at = @At("RETURN"))
    public boolean addWhitelistBypass(boolean original, GameProfile profile) {
        if (original) {
            return true;
        }
        return LoginCache.get(profile.getId(), Permission.BYPASS_WHITELIST) == Tristate.TRUE;
    }
}