package net.p1nero.ss.item;

import net.minecraft.world.item.*;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.p1nero.ss.SwordSoaring;
import org.jetbrains.annotations.NotNull;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SwordSoaring.MOD_ID);
    public static final RegistryObject<Item> LOONG_ROAR = ITEMS.register("loong_roar", () -> new ModWeaponItem(Tiers.NETHERITE, 0, -2, (new Item.Properties()).rarity(Rarity.EPIC)));

}