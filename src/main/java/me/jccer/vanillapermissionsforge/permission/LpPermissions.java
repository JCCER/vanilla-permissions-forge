package me.jccer.vanillapermissionsforge.permission;

import com.mojang.logging.LogUtils;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.platform.PlayerAdapter;
import net.luckperms.api.util.Tristate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.rcon.RconConsoleSource;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

import java.util.OptionalInt;

/**
 * Bridge to the LuckPerms public API with "undefined falls back to vanilla" semantics.
 * LuckPerms is optional: until it is available (or if absent) every check returns UNDEFINED.
 */
public final class LpPermissions {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static volatile boolean available = false;
    private static volatile boolean warned = false;

    private LpPermissions() {}

    /** Whether the LuckPerms provider is available (lazily initialized). */
    public static boolean isAvailable() {
        return ensureAvailable();
    }

    private static boolean ensureAvailable() {
        if (available) {
            return true;
        }
        synchronized (LpPermissions.class) {
            if (available) {
                return true;
            }
            try {
                LuckPerms api = LuckPermsProvider.get();
                api.getServerName();
                available = true;
                LOGGER.info("Connected to LuckPerms {}", api.getPluginMetadata().getVersion());
            } catch (Throwable t) {
                // LuckPerms may not have registered its provider yet (mod construction order);
                // keep retrying lazily. Checks fall back to vanilla until it loads.
                if (!warned) {
                    warned = true;
                    LOGGER.warn("LuckPerms not available yet; vanilla permission checks fall back to vanilla behavior until it loads.");
                }
            }
            return available;
        }
    }

    /**
     * Resolve a permission for a command source.
     * Players (including {@code execute as <player>}) are checked against LuckPerms;
     * console/RCon are always allowed; command blocks, functions and non-player entities
     * return UNDEFINED so vanilla behavior is preserved.
     */
    public static Tristate check(CommandSourceStack source, String node) {
        ServerPlayer player = source.getPlayer();
        if (player != null) {
            return check(player, node);
        }
        if (source.getEntity() == null
                && (source.source instanceof MinecraftServer || source.source instanceof RconConsoleSource)) {
            return Tristate.TRUE;
        }
        return Tristate.UNDEFINED;
    }

    public static Tristate check(ServerPlayer player, String node) {
        if (!ensureAvailable()) {
            return Tristate.UNDEFINED;
        }
        try {
            PlayerAdapter<Player> adapter = LuckPermsProvider.get().getPlayerAdapter(Player.class);
            return adapter.getPermissionData(player).checkPermission(node);
        } catch (Throwable t) {
            return Tristate.UNDEFINED;
        }
    }

    /** Tri-state check falling back to the given default when undefined. */
    public static boolean checkOr(CommandSourceStack source, String node, boolean fallback) {
        Tristate state = check(source, node);
        return state == Tristate.UNDEFINED ? fallback : state == Tristate.TRUE;
    }

    /** Tri-state check for a player, falling back to the given default when undefined. */
    public static boolean checkOr(ServerPlayer player, String node, boolean fallback) {
        Tristate state = check(player, node);
        return state == Tristate.UNDEFINED ? fallback : state == Tristate.TRUE;
    }

    /** Tri-state check for an arbitrary entity; only players have permission data. */
    public static Tristate check(net.minecraft.world.entity.Entity entity, String node) {
        if (entity instanceof ServerPlayer player) {
            return check(player, node);
        }
        return Tristate.UNDEFINED;
    }

    /** Tri-state check for an entity, falling back to the given default when undefined. */
    public static boolean checkOr(net.minecraft.world.entity.Entity entity, String node, boolean fallback) {
        Tristate state = check(entity, node);
        return state == Tristate.UNDEFINED ? fallback : state == Tristate.TRUE;
    }

    /** Integer meta value (option) of a player; unset or invalid values are treated as absent. */
    public static OptionalInt metaInt(ServerPlayer player, String key) {
        if (!ensureAvailable()) {
            return OptionalInt.empty();
        }
        try {
            PlayerAdapter<Player> adapter = LuckPermsProvider.get().getPlayerAdapter(Player.class);
            String value = adapter.getMetaData(player).getMetaValue(key);
            if (value == null) {
                return OptionalInt.empty();
            }
            return OptionalInt.of(Integer.parseInt(value.trim()));
        } catch (Throwable t) {
            return OptionalInt.empty();
        }
    }
}