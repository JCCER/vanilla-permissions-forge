package me.jccer.vanillapermissionsforge.mixin.selector;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.jccer.vanillapermissionsforge.permission.LpPermissions;
import me.jccer.vanillapermissionsforge.util.Permission;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Gates whether a source may use entity selectors at all ({@code selector} node, default = the
 * vanilla/Forge OP check). Forge patches EntitySelector.checkPermissions to call
 * ForgeHooks.canUseEntitySelectors, so that Forge hook is the injection target (not hasPermission).
 */
@Mixin(EntitySelector.class)
public abstract class EntitySelectorMixin {

    @ModifyExpressionValue(
            method = "checkPermissions",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/common/ForgeHooks;canUseEntitySelectors(Lnet/minecraft/commands/SharedSuggestionProvider;)Z"
            )
    )
    public boolean addSelectorPermission(boolean original, CommandSourceStack source) {
        return LpPermissions.checkOr(source, Permission.SELECTOR, original);
    }
}