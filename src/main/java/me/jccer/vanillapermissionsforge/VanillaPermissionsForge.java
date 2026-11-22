package me.jccer.vanillapermissionsforge;

import com.mojang.logging.LogUtils;
import me.jccer.vanillapermissionsforge.command.CommandPermissionHandler;
import me.jccer.vanillapermissionsforge.util.LoginCache;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(VanillaPermissionsForge.MOD_ID)
public class VanillaPermissionsForge {
    public static final String MOD_ID = "vanilla_permissions_forge";
    public static final Logger LOGGER = LogUtils.getLogger();

    public VanillaPermissionsForge() {
        MinecraftForge.EVENT_BUS.register(new CommandPermissionHandler());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        LoginCache.clear(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        LoginCache.clear(event.getEntity().getUUID());
    }
}