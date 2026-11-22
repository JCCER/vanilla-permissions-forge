package me.jccer.vanillapermissionsforge;

import com.mojang.logging.LogUtils;
import me.jccer.vanillapermissionsforge.command.CommandPermissionHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(VanillaPermissionsForge.MOD_ID)
public class VanillaPermissionsForge {
    public static final String MOD_ID = "vanilla_permissions_forge";
    public static final Logger LOGGER = LogUtils.getLogger();

    public VanillaPermissionsForge() {
        MinecraftForge.EVENT_BUS.register(new CommandPermissionHandler());
    }
}