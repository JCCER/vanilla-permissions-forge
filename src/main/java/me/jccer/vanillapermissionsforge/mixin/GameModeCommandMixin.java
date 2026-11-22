package me.jccer.vanillapermissionsforge.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.commands.GameModeCommand;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;

/**
 * Since 22w45a the gamemode command uses GameModeArgument instead of literal children, so per-mode
 * permission nodes (command.gamemode.survival) cannot exist. Cancel the vanilla registration and
 * re-register with a literal node per game mode; LuckPerms then wraps each as command.gamemode.&lt;mode&gt;.
 */
@Mixin(GameModeCommand.class)
public abstract class GameModeCommandMixin {

    @Inject(method = "register", at = @At("HEAD"), cancellable = true)
    private static void registerLiterals(CommandDispatcher<CommandSourceStack> dispatcher, CallbackInfo ci) {
        ci.cancel();

        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("gamemode")
                .requires(source -> source.hasPermission(2));

        for (GameType gameType : GameType.values()) {
            builder.then(
                    Commands.literal(gameType.getName())
                            .executes(context -> GameModeCommandAccessor.invokeSetMode(
                                    context, Collections.singleton(context.getSource().getPlayerOrException()), gameType))
                            .then(
                                    Commands.argument("target", EntityArgument.players())
                                            .executes(context -> GameModeCommandAccessor.invokeSetMode(
                                                    context, EntityArgument.getPlayers(context, "target"), gameType))
                            )
            );
        }

        dispatcher.register(builder);
    }
}