package me.jccer.vanillapermissionsforge.util;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.RootCommandNode;
import me.jccer.vanillapermissionsforge.permission.LpPermissions;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.OptionalInt;

import static net.minecraft.commands.arguments.EntityArgument.ERROR_SELECTORS_NOT_ALLOWED;

/**
 * Validates entity-selector argument results against the {@code selector.*} permissions and meta.
 * Command blocks and datapacks bypass selector checks: the original command source must be a player
 * (so {@code execute as @a} run from console is not restricted). Borrowed from VanillaPermissions.
 */
public final class ArgumentPermission {

    private ArgumentPermission() {}

    public static void validate(CommandContext<CommandSourceStack> context, String selector, Collection<?> selected) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();

        // The original command source must be a player; console/command-block/datapack sources bypass.
        CommandSource commandSource = source.source;
        ServerPlayer attachedPlayer = source.getPlayer();
        if (attachedPlayer == null || !(commandSource instanceof ServerPlayer)) {
            return;
        }

        String[] parts;
        if (context.getRootNode() instanceof RootCommandNode) {
            parts = context.getNodes().stream().map(node -> node.getNode().getName()).toArray(String[]::new);
        } else {
            parts = source.getServer().getCommands().getDispatcher()
                    .getPath(Iterables.getLast(context.getNodes()).getNode()).toArray(String[]::new);
        }
        String name = Permission.build(parts[0], selector, Permission.build(1, parts.length, parts));

        OptionalInt limit = LpPermissions.metaInt(attachedPlayer, Permission.SELECTOR_LIMIT.formatted(name));
        if (limit.isPresent() && limit.getAsInt() < selected.size()) {
            throw ERROR_SELECTORS_NOT_ALLOWED.create();
        }

        boolean entity = LpPermissions.checkOr(source, Permission.SELECTOR_ENTITY.formatted(name), true);
        boolean player = LpPermissions.checkOr(source, Permission.SELECTOR_PLAYER.formatted(name), true);
        boolean self = LpPermissions.checkOr(source, Permission.SELECTOR_SELF.formatted(name), true);

        String weightKey = Permission.SELECTOR_WEIGHT.formatted(name);
        OptionalInt sourceWeight = LpPermissions.metaInt(attachedPlayer, weightKey);
        boolean sourceWeightPresent = sourceWeight.isPresent();
        if (entity && player && self && !sourceWeightPresent) {
            return;
        }
        int sourceWeightValue = sourceWeight.orElse(0);

        GameProfile sourceProfile = attachedPlayer.getGameProfile();
        for (Object object : selected) {
            Object selectedEntity = object;
            if (selectedEntity instanceof Player selectedPlayer) {
                selectedEntity = selectedPlayer.getGameProfile();
            }

            if (selectedEntity instanceof Entity) {
                if (!entity) {
                    throw ERROR_SELECTORS_NOT_ALLOWED.create();
                }
            } else if (selectedEntity instanceof GameProfile selectedProfile) {
                if (selectedProfile.equals(sourceProfile)) {
                    if (!self) {
                        throw ERROR_SELECTORS_NOT_ALLOWED.create();
                    }
                } else {
                    if (!player) {
                        throw ERROR_SELECTORS_NOT_ALLOWED.create();
                    }
                    if (sourceWeightPresent && object instanceof ServerPlayer onlinePlayer) {
                        OptionalInt selectedWeight = LpPermissions.metaInt(onlinePlayer, weightKey);
                        if (selectedWeight.isPresent() && selectedWeight.getAsInt() > sourceWeightValue) {
                            throw ERROR_SELECTORS_NOT_ALLOWED.create();
                        }
                    }
                }
            } else {
                throw ERROR_SELECTORS_NOT_ALLOWED.create();
            }
        }
    }
}