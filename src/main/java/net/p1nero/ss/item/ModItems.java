package net.p1nero.ss.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.p1nero.ss.SwordSoaring;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SwordSoaring.MOD_ID);
    public static final RegistryObject<Item> SWORD_SOARING_SECRET = ITEMS.register("sword_soaring_secret", SwordSoaringSecret::new);

}
