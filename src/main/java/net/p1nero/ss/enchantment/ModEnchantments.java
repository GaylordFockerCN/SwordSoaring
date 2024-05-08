package net.p1nero.ss.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.p1nero.ss.SwordSoaring;

public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, SwordSoaring.MOD_ID);

    public static RegistryObject<Enchantment> SWORD_SOARING =
            ENCHANTMENTS.register("sword_soaring",
                    () -> new SwordSoaringEnchantment(Enchantment.Rarity.RARE,
                            EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND}));

}