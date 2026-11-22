package me.jccer.vanillapermissionsforge.mixin.operatorblocks;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.jccer.vanillapermissionsforge.mixin.accessor.ServerGamePacketListenerImplAccessor;
import me.jccer.vanillapermissionsforge.permission.LpPermissions;
import me.jccer.vanillapermissionsforge.util.Permission;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Editing command/jigsaw/structure blocks and command-block minecarts (and generating jigsaw
 * structures) is gated by the operator_block.&lt;block&gt;.edit node. Consolidated into one mixin
 * for the packet listener rather than one class per packet.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class CommandBlockEditMixin {

    @ModifyExpressionValue(
            method = "handleSetCommandBlock",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;canUseGameMasterBlocks()Z")
    )
    public boolean addCommandBlockEdit(boolean original) {
        return edit(Blocks.COMMAND_BLOCK, original);
    }

    @ModifyExpressionValue(
            method = "handleSetJigsawBlock",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;canUseGameMasterBlocks()Z")
    )
    public boolean addJigsawBlockEdit(boolean original) {
        return edit(Blocks.JIGSAW, original);
    }

    @ModifyExpressionValue(
            method = "handleJigsawGenerate",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;canUseGameMasterBlocks()Z")
    )
    public boolean addJigsawGenerate(boolean original) {
        return edit(Blocks.JIGSAW, original);
    }

    @ModifyExpressionValue(
            method = "handleSetStructureBlock",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;canUseGameMasterBlocks()Z")
    )
    public boolean addStructureBlockEdit(boolean original) {
        return edit(Blocks.STRUCTURE_BLOCK, original);
    }

    @ModifyExpressionValue(
            method = "handleSetCommandMinecart",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;canUseGameMasterBlocks()Z")
    )
    public boolean addCommandBlockMinecartEdit(boolean original) {
        String path = BuiltInRegistries.ITEM.getKey(Items.COMMAND_BLOCK_MINECART).getPath();
        return LpPermissions.checkOr(player(), Permission.OPERATOR_BLOCK_EDIT.formatted(path), original);
    }

    private boolean edit(Block block, boolean original) {
        String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
        return LpPermissions.checkOr(player(), Permission.OPERATOR_BLOCK_EDIT.formatted(path), original);
    }

    private ServerPlayer player() {
        return ((ServerGamePacketListenerImplAccessor) this).getPlayer();
    }
}