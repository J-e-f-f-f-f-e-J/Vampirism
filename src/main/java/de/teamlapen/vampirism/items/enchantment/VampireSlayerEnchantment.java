package de.teamlapen.vampirism.items.enchantment;

import de.teamlapen.vampirism.core.tags.ModEntityTags;
import de.teamlapen.vampirism.core.tags.ModItemTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;

import javax.annotation.Nullable;
import java.util.Optional;

public class VampireSlayerEnchantment extends DamageEnchantment {

    public VampireSlayerEnchantment() {
        super(Enchantment.definition(
                        ItemTags.WEAPON_ENCHANTABLE,
                        ModItemTags.VAMPIRE_SLAYER_ITEMS,
                        5,
                        5,
                        Enchantment.dynamicCost(3, 10),
                        Enchantment.dynamicCost(20, 10),
                        2,
                        EquipmentSlot.MAINHAND),
                Optional.of(ModEntityTags.VAMPIRE));
    }

    @Override
    public float getDamageBonus(int level, @Nullable EntityType<?> entityType) {
        boolean effective = entityType != null && (this.targets.map(entityType::is).orElse(false));
        return effective ? 2f + (float) level : 0.0F;
    }
}
