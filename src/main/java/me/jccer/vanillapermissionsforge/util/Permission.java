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

    public static final String BYPASS_SPAWN_PROTECTION = "bypass.spawn-protection";
    public static final String BYPASS_FORCE_GAMEMODE = "bypass.force-gamemode";
    public static final String BYPASS_MOVE_SPEED_PLAYER = "bypass.move-speed.player";
    public static final String BYPASS_MOVE_SPEED_VEHICLE = "bypass.move-speed.vehicle.%s.%s";
    public static final String BYPASS_CHAT_SPEED = "bypass.chat-speed";
    public static final String BYPASS_WHITELIST = "bypass.whitelist";
    public static final String BYPASS_PLAYER_LIMIT = "bypass.player-limit";

    public static final String OPERATOR_BLOCK_PLACE = "operator_block.%s.place";
    public static final String OPERATOR_BLOCK_VIEW = "operator_block.%s.view";
    public static final String OPERATOR_BLOCK_EDIT = "operator_block.%s.edit";
    public static final String OPERATOR_BLOCK_BREAK = "operator_block.%s.break";

    public static final String NBT_QUERY_ENTITY = "nbt.query.entity";
    public static final String NBT_QUERY_BLOCK = "nbt.query.block";
    public static final String NBT_LOAD_ENTITY = "nbt.load.entity";
    public static final String NBT_LOAD_BLOCK = "nbt.load.block";

    public static final String DEBUG_STICK_USE = "debug_stick.use.%s.%s";
    public static final String ADMIN_BROADCAST_RECEIVE = "adminbroadcast.receive";

    private Permission() {}

    public static String build(String... parts) {
        return String.join(".", parts);
    }

    public static String build(int startIndex, int endIndex, String... parts) {
        return String.join(".", Arrays.copyOfRange(parts, startIndex, endIndex));
    }
}