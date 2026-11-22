package me.jccer.vanillapermissionsforge.command;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.jccer.vanillapermissionsforge.VanillaPermissionsForge;
import me.jccer.vanillapermissionsforge.permission.LpPermissions;
import net.luckperms.api.util.Tristate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * General fine-grained command permission control. Wraps every command node — literals and
 * arguments — with a LuckPerms node named after its full structural path
 * ({@code command.<root>.<...>.<argumentName>}), using the {@code command.} convention LuckPerms
 * itself uses. An explicit true allows absolutely, false denies, undefined defers to the original
 * requirement (vanilla or mod).
 *
 * <p>Two passes: {@link AddReloadListenerEvent} (lowest priority, after LuckPerms' own injector)
 * complements the registered tree and re-fires on /reload so wrapping survives reloads;
 * {@link ServerStartedEvent} (lowest priority) additionally catches commands registered late.
 * Literals LuckPerms already wraps are left to it (no redundant wrapping); argument nodes always
 * receive the finer argument-name node layered on top. Brigadier lives in a boot module layer that
 * Mixin cannot transform, so requirements are set via reflection.
 */
public final class CommandPermissionHandler {

    private static final Field REQUIREMENT_FIELD;

    static {
        try {
            REQUIREMENT_FIELD = CommandNode.class.getDeclaredField("requirement");
            REQUIREMENT_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onAddReloadListener(AddReloadListenerEvent event) {
        wrapTree(event.getServerResources().getCommands().getDispatcher().getRoot());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onServerStarted(ServerStartedEvent event) {
        wrapTree(event.getServer().getCommands().getDispatcher().getRoot());
    }

    private void wrapTree(CommandNode<CommandSourceStack> root) {
        int wrapped = 0;
        for (CommandNode<CommandSourceStack> child : root.getChildren()) {
            wrapped += wrapNode("", child);
        }
        if (wrapped > 0) {
            VanillaPermissionsForge.LOGGER.info("Wrapped {} command node(s) with LuckPerms permissions", wrapped);
        }
    }

    private int wrapNode(String path, CommandNode<CommandSourceStack> node) {
        String name = node.getName().toLowerCase(Locale.ROOT);
        String nodePath = path.isEmpty() ? name : path + "." + name;

        int wrapped = 0;
        for (CommandNode<CommandSourceStack> child : node.getChildren()) {
            wrapped += wrapNode(nodePath, child);
        }

        if (shouldWrap(node)) {
            setRequirement(node, new VpRequirement("command." + nodePath, node.getRequirement()));
            wrapped++;
        }
        return wrapped;
    }

    private static boolean shouldWrap(CommandNode<CommandSourceStack> node) {
        Predicate<CommandSourceStack> requirement = node.getRequirement();
        if (requirement instanceof VpRequirement) {
            return false; // already wrapped by us (idempotent across the two passes)
        }
        boolean lpWrapped = requirement.getClass().getName().startsWith("me.lucko.luckperms");
        // Leave LuckPerms-wrapped literals to LuckPerms; always add the finer node for arguments.
        return !(node instanceof LiteralCommandNode && lpWrapped);
    }

    private static void setRequirement(CommandNode<CommandSourceStack> node, Predicate<CommandSourceStack> requirement) {
        try {
            REQUIREMENT_FIELD.set(node, requirement);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /** Named wrapper so repeated tree walks can recognize and skip our own nodes. */
    private static final class VpRequirement implements Predicate<CommandSourceStack> {
        private final String permission;
        private final Predicate<CommandSourceStack> fallback;

        VpRequirement(String permission, Predicate<CommandSourceStack> fallback) {
            this.permission = permission;
            this.fallback = fallback;
        }

        @Override
        public boolean test(CommandSourceStack source) {
            Tristate state = LpPermissions.check(source, permission);
            if (state == Tristate.TRUE) {
                return true;
            }
            if (state == Tristate.FALSE) {
                return false;
            }
            return fallback.test(source);
        }
    }
}