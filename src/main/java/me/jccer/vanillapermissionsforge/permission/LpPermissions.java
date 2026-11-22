package me.jccer.vanillapermissionsforge.permission;

import com.mojang.logging.LogUtils;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import net.luckperms.api.util.Tristate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.rcon.RconConsoleSource;
import org.slf4j.Logger;

import java.util.OptionalInt;

/**
 * Bridge to the LuckPerms public API with "undefined falls back to vanilla" semantics.
 * Uses UserManager + ContextManager (the same path LuckPerms' own BrigadierInjector uses),
 * which resolves the player's loaded data in their current context. LuckPerms is optional:
 * until it is available (or if absent) every check returns UNDEFINED.
 */
public final class LpPermissions {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static volatile boolean available = false;
    private static volatile boolean warned = false;

    private LpPermissions() {}

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
                if (!warned) {
                    warned = true;
                    LOGGER.warn("LuckPerms not available yet; permission checks fall back to vanilla until it loads.");
                }
            }
            return available;
        }
    }

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
            LuckPerms lp = LuckPermsProvider.get();
            User user = lp.getUserManager().getUser(player.getUUID());
            if (user == null) {
                return Tristate.UNDEFINED;
            }
            QueryOptions queryOptions = lp.getContextManager().getQueryOptions(player);
            return user.getCachedData().getPermissionData(queryOptions).checkPermission(node);
        } catch (Throwable t) {
            return Tristate.UNDEFINED;
        }
    }

    public static boolean checkOr(CommandSourceStack source, String node, boolean fallback) {
        Tristate state = check(source, node);
        return state == Tristate.UNDEFINED ? fallback : state == Tristate.TRUE;
    }

    public static boolean checkOr(ServerPlayer player, String node, boolean fallback) {
        Tristate state = check(player, node);
        return state == Tristate.UNDEFINED ? fallback : state == Tristate.TRUE;
    }

    public static Tristate check(net.minecraft.world.entity.Entity entity, String node) {
        if (entity instanceof ServerPlayer player) {
            return check(player, node);
        }
        return Tristate.UNDEFINED;
    }

    public static boolean checkOr(net.minecraft.world.entity.Entity entity, String node, boolean fallback) {
        Tristate state = check(entity, node);
        return state == Tristate.UNDEFINED ? fallback : state == Tristate.TRUE;
    }

    public static OptionalInt metaInt(ServerPlayer player, String key) {
        if (!ensureAvailable()) {
            return OptionalInt.empty();
        }
        try {
            LuckPerms lp = LuckPermsProvider.get();
            User user = lp.getUserManager().getUser(player.getUUID());
            if (user == null) {
                return OptionalInt.empty();
            }
            QueryOptions queryOptions = lp.getContextManager().getQueryOptions(player);
            String value = user.getCachedData().getMetaData(queryOptions).getMetaValue(key);
            if (value == null) {
                return OptionalInt.empty();
            }
            return OptionalInt.of(Integer.parseInt(value.trim()));
        } catch (Throwable t) {
            return OptionalInt.empty();
        }
    }
}