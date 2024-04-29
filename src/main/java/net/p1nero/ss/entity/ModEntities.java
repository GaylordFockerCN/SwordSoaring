package net.p1nero.ss.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.p1nero.ss.SwordSoaring;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SwordSoaring.MOD_ID);
    public static final RegistryObject<EntityType<SwordEntity>> SWORD = register("sword",
            EntityType.Builder.of(SwordEntity::new,  MobCategory.CREATURE));
    private static <T extends Entity> RegistryObject<EntityType<T>> register(String registryname, EntityType.Builder<T> entityTypeBuilder) {
        return REGISTRY.register(registryname, () -> entityTypeBuilder.build(new ResourceLocation(SwordSoaring.MOD_ID, registryname).toString()));
    }

}
