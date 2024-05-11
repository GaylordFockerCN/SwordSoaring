package net.p1nero.ss.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.p1nero.ss.SwordSoaring;
import org.jetbrains.annotations.NotNull;

public class SwordSoaringEnchantment extends Enchantment {
    protected SwordSoaringEnchantment(Rarity p_44676_, EnchantmentCategory p_44677_, EquipmentSlot[] p_44678_) {
        super(p_44676_, p_44677_, p_44678_);
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    public static float getScale(int level){
        return switch (level){
            case 1 -> 0.75f;
            case 2 -> 0.5f;
            default -> 1;
        };
    }

    @Override
    public boolean canEnchant(@NotNull ItemStack itemStack) {
        return SwordSoaring.isValidSword(itemStack);
    }
}
