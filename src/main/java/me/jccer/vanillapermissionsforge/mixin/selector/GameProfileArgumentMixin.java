package me.jccer.vanillapermissionsforge.mixin.selector;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.jccer.vanillapermissionsforge.util.ArgumentPermission;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.GameProfileArgument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(GameProfileArgument.class)
public abstract class GameProfileArgumentMixin {

    @Inject(method = "getGameProfiles", at = @At("RETURN"))
    private static void addGameProfilesPermission(CommandContext<CommandSourceStack> context, String name, CallbackInfoReturnable<Collection<GameProfile>> cir) throws CommandSyntaxException {
        ArgumentPermission.validate(context, name, cir.getReturnValue());
    }
}