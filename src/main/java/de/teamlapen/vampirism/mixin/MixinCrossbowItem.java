package de.teamlapen.vampirism.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.teamlapen.vampirism.api.items.IVampirismCrossbow;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CrossbowItem.class)
public abstract class MixinCrossbowItem {


    @ModifyReturnValue(method = "getChargeDuration", at = @At("RETURN"))
    private static int modifyCharge(int original, ItemStack crossbowStack) {
        if (crossbowStack.getItem() instanceof IVampirismCrossbow crossbow) {
            return crossbow.getChargeDurationMod(crossbowStack);
        }
        return original;
    }
}
