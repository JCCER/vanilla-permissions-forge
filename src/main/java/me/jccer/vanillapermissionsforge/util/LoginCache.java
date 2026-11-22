package me.jccer.vanillapermissionsforge.util;

import com.mojang.logging.LogUtils;
import me.jccer.vanillapermissionsforge.permission.LpPermissions;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.util.Tristate;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches the login-time bypass nodes per player. These checks (whitelist, player limit, forced
 * game mode) run during login before LuckPerms has loaded the user, so the user is loaded
 * explicitly here and the relevant nodes cached. Cleared on login/logout to stay fresh and bounded.
 */
public final class LoginCache {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String[] NODES = {
            Permission.BYPASS_WHITELIST,
            Permission.BYPASS_PLAYER_LIMIT,
            Permission.BYPASS_FORCE_GAMEMODE
    };

    private static final Map<UUID, Map<String, Tristate>> CACHE = new ConcurrentHashMap<>();

    private LoginCache() {}

    public static Tristate get(UUID uuid, String node) {
        return CACHE.computeIfAbsent(uuid, LoginCache::load).getOrDefault(node, Tristate.UNDEFINED);
    }

    private static Map<String, Tristate> load(UUID uuid) {
        Map<String, Tristate> values = new HashMap<>();
        if (!LpPermissions.isAvailable()) {
            return values;
        }
        try {
            User user = LuckPermsProvider.get().getUserManager().loadUser(uuid).join();
            for (String node : NODES) {
                values.put(node, user.getCachedData().getPermissionData().checkPermission(node));
            }
        } catch (Throwable t) {
            LOGGER.warn("Failed to load login permissions for {}", uuid, t);
        }
        return values;
    }

    public static void clear(UUID uuid) {
        CACHE.remove(uuid);
    }
}