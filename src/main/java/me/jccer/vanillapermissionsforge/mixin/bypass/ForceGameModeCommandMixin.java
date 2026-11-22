package me.jccer.vanillapermissionsforge.mixin.bypass;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.jccer.vanillapermissionsforge.permission.LpPermissions;
import me.jccer.vanillapermissionsforge.util.Permission;
import net.minecraft.server.commands.DefaultGameModeCommands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * /defaultgamemode skips players with bypass.force-gamemode so their game mode is not overridden.
 * Runtime path: the player is online, so a live LuckPerms check is used.
 */
@Mixin(DefaultGameModeCommands.class)
public abstract class ForceGameModeCommandMixin {

    @WrapOperation(
            method = "setMode",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setGameMode(Lnet/minecraft/world/level/GameType;)Z")
    )
    private static boolean skipForcedGameMode(ServerPlayer player, GameType gameType, Operation<Boolean> original) {
        if (LpPermissions.checkOr(player, Permission.BYPASS_FORCE_GAMEMODE, false)) {
            return false;
        }
        return original.call(player, gameType);
    }
}