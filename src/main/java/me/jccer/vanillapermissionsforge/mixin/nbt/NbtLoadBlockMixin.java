package me.jccer.vanillapermissionsforge.mixin.nbt;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.jccer.vanillapermissionsforge.permission.LpPermissions;
import me.jccer.vanillapermissionsforge.util.Permission;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockItem.class)
public abstract class NbtLoadBlockMixin {

    @ModifyExpressionValue(
            method = "updateCustomBlockEntityTag(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;canUseGameMasterBlocks()Z")
    )
    private static boolean addNbtLoadBlock(boolean original, Level level, Player player, BlockPos pos, ItemStack stack) {
        if (player instanceof ServerPlayer serverPlayer) {
            return LpPermissions.checkOr(serverPlayer, Permission.NBT_LOAD_BLOCK, original);
        }
        return original;
    }
}