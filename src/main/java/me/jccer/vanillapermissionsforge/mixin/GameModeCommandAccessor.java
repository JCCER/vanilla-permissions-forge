package me.jccer.vanillapermissionsforge.mixin;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.GameModeCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Collection;

@Mixin(GameModeCommand.class)
public interface GameModeCommandAccessor {
    @Invoker("setMode")
    static int invokeSetMode(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players, GameType gameType) {
        throw new AssertionError();
    }
}