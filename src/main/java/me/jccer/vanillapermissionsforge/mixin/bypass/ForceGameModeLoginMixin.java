package me.jccer.vanillapermissionsforge.mixin.bypass;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.jccer.vanillapermissionsforge.util.LoginCache;
import me.jccer.vanillapermissionsforge.util.Permission;
import net.luckperms.api.util.Tristate;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * On login, skip the server's forced game mode for players with bypass.force-gamemode, so they
 * keep their own game mode. Uses the login cache (the user is not fully online yet).
 */
@Mixin(ServerPlayer.class)
public abstract class ForceGameModeLoginMixin {

    @WrapOperation(
            method = "calculateGameModeForNewPlayer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getForcedGameType()Lnet/minecraft/world/level/GameType;")
    )
    public GameType skipForcedGameMode(MinecraftServer server, Operation<GameType> original) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        if (LoginCache.get(self.getUUID(), Permission.BYPASS_FORCE_GAMEMODE) == Tristate.TRUE) {
            return null;
        }
        return original.call(server);
    }
}