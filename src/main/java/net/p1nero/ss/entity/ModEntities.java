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
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SwordSoaring.MOD_ID);
    public static final RegistryObject<EntityType<SwordEntity>> SWORD = register("sword",
            EntityType.Builder.of(SwordEntity::new, MobCategory.CREATURE));

    public static final RegistryObject<EntityType<RainScreenSwordEntity>> RAIN_SCREEN_SWORD = register("rain_screen_sword",
            EntityType.Builder.of(RainScreenSwordEntity::new, MobCategory.CREATURE));

//    public static final RegistryObject<EntityType<RainCutterSwordEntity>> RAIN_CUTTER_SWORD = register("rain_cutter_sword",
//            EntityType.Builder.of(RainCutterSwordEntity::new, MobCategory.CREATURE), 1, 0.5f);
    /**
     * 这样构造出来的实体发射才不会卡，正常的方法构造会卡。但是又对雨帘剑不起作用、、、
     */
    public static final RegistryObject<EntityType<RainCutterSwordEntity>> RAIN_CUTTER_SWORD = register("rain_cutter_sword", EntityType.Builder.<RainCutterSwordEntity>of(RainCutterSwordEntity::new, MobCategory.MISC)
            .setCustomClientFactory(RainCutterSwordEntity::new).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(1f, 0.2f));

    public static final RegistryObject<EntityType<StellarSwordEntity>> STELLAR_SWORD = register("stellar_sword", EntityType.Builder.<StellarSwordEntity>of(StellarSwordEntity::new, MobCategory.MISC)
            .setCustomClientFactory(StellarSwordEntity::new).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(1f, 0.2f));

    private static <T extends Entity> RegistryObject<EntityType<T>> register(String registryname, EntityType.Builder<T> entityTypeBuilder) {
        return ENTITIES.register(registryname, () -> entityTypeBuilder.build(new ResourceLocation(SwordSoaring.MOD_ID, registryname).toString()));
    }
    private static <T extends Entity> RegistryObject<EntityType<T>> register(String registryname, EntityType.Builder<T> entityTypeBuilder, float xz, float y) {
        return ENTITIES.register(registryname, () -> entityTypeBuilder.sized(xz,y).build(new ResourceLocation(SwordSoaring.MOD_ID, registryname).toString()));
    }

}
