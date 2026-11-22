package me.jccer.vanillapermissionsforge.util;

import java.util.Arrays;

/**
 * Permission node constants. Nodes use LuckPerms-friendly names without the {@code minecraft.}
 * prefix, consistent with the {@code command.} convention used for command nodes.
 */
public final class Permission {
    public static final String SELECTOR = "selector";
    public static final String SELECTOR_ENTITY = "selector.entity.%s";
    public static final String SELECTOR_PLAYER = "selector.player.%s";
    public static final String SELECTOR_SELF = "selector.self.%s";
    public static final String SELECTOR_LIMIT = "selector.limit.%s";
    public static final String SELECTOR_WEIGHT = "selector.weight.%s";

    private Permission() {}

    public static String build(String... parts) {
        return String.join(".", parts);
    }

    public static String build(int startIndex, int endIndex, String... parts) {
        return String.join(".", Arrays.copyOfRange(parts, startIndex, endIndex));
    }
}