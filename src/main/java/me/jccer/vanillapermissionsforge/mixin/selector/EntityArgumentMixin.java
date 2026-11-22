package me.jccer.vanillapermissionsforge.mixin.selector;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.jccer.vanillapermissionsforge.util.ArgumentPermission;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

import static java.util.Collections.singleton;

@Mixin(EntityArgument.class)
public abstract class EntityArgumentMixin {

    @Inject(method = {"getEntity", "getPlayer"}, at = @At("RETURN"))
    private static void addEntityPermission(CommandContext<CommandSourceStack> context, String name, CallbackInfoReturnable<Entity> cir) throws CommandSyntaxException {
        ArgumentPermission.validate(context, name, singleton(cir.getReturnValue()));
    }

    @Inject(method = {"getEntities", "getOptionalEntities", "getPlayers", "getOptionalPlayers"}, at = @At("RETURN"))
    private static void addEntitiesPermission(CommandContext<CommandSourceStack> context, String name, CallbackInfoReturnable<Collection<Entity>> cir) throws CommandSyntaxException {
        ArgumentPermission.validate(context, name, cir.getReturnValue());
    }
}