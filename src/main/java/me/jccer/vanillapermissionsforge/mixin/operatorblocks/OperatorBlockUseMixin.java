package me.jccer.vanillapermissionsforge.mixin.operatorblocks;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.jccer.vanillapermissionsforge.permission.LpPermissions;
import me.jccer.vanillapermissionsforge.util.Permission;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({CommandBlock.class, JigsawBlock.class})
public abstract class OperatorBlockUseMixin extends Block {

    public OperatorBlockUseMixin(Properties properties) {
        super(properties);
    }

    @ModifyExpressionValue(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;canUseGameMasterBlocks()Z")
    )
    public boolean addOperatorBlockView(boolean original, BlockState state, Level level, BlockPos pos, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            return LpPermissions.checkOr(serverPlayer, Permission.OPERATOR_BLOCK_VIEW.formatted(BuiltInRegistries.BLOCK.getKey(this).getPath()), original);
        }
        return original;
    }
}