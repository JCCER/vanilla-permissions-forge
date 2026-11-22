package me.jccer.vanillapermissionsforge.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.jccer.vanillapermissionsforge.mixin.accessor.CommandSourceStackAccessor;
import me.jccer.vanillapermissionsforge.mixin.accessor.ServerPlayerAccessor;
import me.jccer.vanillapermissionsforge.permission.LpPermissions;
import net.luckperms.api.util.Tristate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.ExecuteCommand;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.arguments.EntityArgument.entities;

/**
 * Adds datapack-friendly execute subcommands (borrowed from VanillaPermissions, Forge-native):
 * {@code execute (if|unless) permission <entity> <node>}, {@code execute feedback (entity <targets>|
 * silent|console)}, and {@code execute oplevel (entity <targets>|<0-4>)} which lowers the execution
 * context's op level (never raises it). The permission conditional mirrors vanilla's addConditional
 * structure directly, avoiding its private CommandPredicate type (no access transformer needed).
 */
@Mixin(ExecuteCommand.class)
public abstract class ExecuteCommandMixin {

    private static final SimpleCommandExceptionType ERROR_PERMISSION_CONDITIONAL_FAILED =
            new SimpleCommandExceptionType(Component.translatable("commands.execute.conditional.fail"));

    @Inject(
            method = "addConditionals",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;then(Lcom/mojang/brigadier/builder/ArgumentBuilder;)Lcom/mojang/brigadier/builder/ArgumentBuilder;",
                    remap = false,
                    ordinal = 0
            )
    )
    private static void addPermissionConditional(CommandNode<CommandSourceStack> node, LiteralArgumentBuilder<CommandSourceStack> argumentBuilder, boolean positive, CommandBuildContext buildContext, CallbackInfoReturnable<ArgumentBuilder<CommandSourceStack, ?>> cir) {
        argumentBuilder.then(
                literal("permission").then(
                        argument("entity", EntityArgument.entity()).then(
                                permissionConditional(node, argument("permission", word()), positive)
                        )
                )
        );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> permissionConditional(CommandNode<CommandSourceStack> node, ArgumentBuilder<CommandSourceStack, ?> builder, boolean positive) {
        builder.fork(node, context -> expectPermission(context, positive))
                .executes(context -> {
                    expectPermission(context, positive);
                    return 1;
                });
        return builder;
    }

    private static Collection<CommandSourceStack> expectPermission(CommandContext<CommandSourceStack> context, boolean positive) throws CommandSyntaxException {
        boolean has = LpPermissions.check(EntityArgument.getEntity(context, "entity"), getString(context, "permission")) == Tristate.TRUE;
        if (has != positive) {
            throw ERROR_PERMISSION_CONDITIONAL_FAILED.create();
        }
        return Collections.singleton(context.getSource());
    }

    @WrapOperation(
            method = "register",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;then(Lcom/mojang/brigadier/builder/ArgumentBuilder;)Lcom/mojang/brigadier/builder/ArgumentBuilder;",
                    remap = false,
                    ordinal = 0
            )
    )
    private static ArgumentBuilder<CommandSourceStack, ?> addModifiers(LiteralArgumentBuilder<CommandSourceStack> instance, ArgumentBuilder<CommandSourceStack, ?> argumentBuilder, Operation<ArgumentBuilder<CommandSourceStack, ?>> original, @Local LiteralCommandNode<CommandSourceStack> root) {
        instance.then(
                literal("feedback").then(
                        literal("entity").then(
                                argument("targets", entities()).fork(root, context -> {
                                    List<CommandSourceStack> list = new ArrayList<>();
                                    ((CommandSourceStackAccessor) context.getSource()).setSilent(false);
                                    for (Entity entity : EntityArgument.getOptionalEntities(context, "targets")) {
                                        list.add(context.getSource().withSource(entity));
                                    }
                                    return list;
                                })
                        )
                ).then(
                        literal("silent").redirect(root, context -> context.getSource().withSuppressedOutput())
                ).then(
                        literal("console").redirect(root, context -> {
                            ((CommandSourceStackAccessor) context.getSource()).setSilent(false);
                            return context.getSource().withSource(context.getSource().getServer());
                        })
                )
        ).then(
                literal("oplevel").then(
                        literal("entity").then(
                                argument("targets", entities()).fork(root, context -> {
                                    List<CommandSourceStack> list = new ArrayList<>();
                                    for (Entity entity : EntityArgument.getOptionalEntities(context, "targets")) {
                                        int originalLevel = ((CommandSourceStackAccessor) context.getSource()).getPermissionLevel();
                                        int newLevel = 0;
                                        if (entity instanceof ServerPlayerAccessor accessor) {
                                            newLevel = accessor.invokeGetPermissionLevel();
                                        }
                                        list.add(context.getSource().withPermission(Math.min(originalLevel, newLevel)));
                                    }
                                    return list;
                                })
                        )
                ).then(
                        argument("level", integer(0, 4)).redirect(root, context -> {
                            int originalLevel = ((CommandSourceStackAccessor) context.getSource()).getPermissionLevel();
                            int newLevel = getInteger(context, "level");
                            return context.getSource().withPermission(Math.min(originalLevel, newLevel));
                        })
                )
        );
        return original.call(instance, argumentBuilder);
    }
}