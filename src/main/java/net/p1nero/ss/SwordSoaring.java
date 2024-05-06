package net.p1nero.ss;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.p1nero.ss.capability.SSCapabilityProvider;
import net.p1nero.ss.capability.SSPlayer;
import net.p1nero.ss.entity.ModEntities;
import net.p1nero.ss.entity.SwordEntityRenderer;
import net.p1nero.ss.epicfight.ModSkills;
import net.p1nero.ss.item.ModItems;
import net.p1nero.ss.network.PacketHandler;
import yesman.epicfight.config.ConfigManager;
import yesman.epicfight.data.loot.function.SetSkillFunction;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.item.EpicFightItems;

@Mod(SwordSoaring.MOD_ID)
public class SwordSoaring {

    public static final String MOD_ID = "sword_soaring";

    public SwordSoaring(){
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus fg_bus = MinecraftForge.EVENT_BUS;
        MinecraftForge.EVENT_BUS.register(this);
        ModEntities.REGISTRY.register(bus);
        ModItems.REGISTRY.register(bus);
        bus.addListener(this::commonSetup);
        if(ModList.get().isLoaded("epicfight")){
            fg_bus.addListener(ModSkills::BuildSkills);
        }

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, net.p1nero.ss.Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        PacketHandler.register();
        if(ModList.get().isLoaded("epicfight")){
            ModSkills.registerSkills();
        }
    }
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents{
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event){
            EntityRenderers.register(ModEntities.SWORD.get(), SwordEntityRenderer::new);
        }

    }

    @Mod.EventBusSubscriber(modid = MOD_ID)
    public static class ModEvents{

        /**
         * 控制飞行和耐力消耗
         */
        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            Player player = event.player;
            if(ModList.get().isLoaded("epicfight") && player.getCapability(SSCapabilityProvider.SS_PLAYER).orElse(new SSPlayer()).isFlying()){
                player.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).ifPresent((entityPatch)->{
                    if(entityPatch instanceof PlayerPatch<?> playerPatch){
                        player.setDeltaMovement(player.getViewVector(0.5f).scale(Config.FLY_SPEED_SCALE.get()));
                        playerPatch.consumeStamina(Config.STAMINA_CONSUME_PER_TICK.get().floatValue());
                    }
                });
            }
        }

        /**
         * 把技能书加到箱子里
         */
        @SubscribeEvent
        public static void modifyVanillaLootPools(final LootTableLoadEvent event) {
            if(!ModList.get().isLoaded("epicfight")){
                return;
            }

            int modifier = ConfigManager.SKILL_BOOK_CHEST_LOOT_MODIFYER.get();
            int dropChance = 100 + modifier;
            int antiDropChance = 100 - modifier;
            float dropChanceModifier = dropChance / (float) (antiDropChance + dropChance);

            if (event.getName().equals(BuiltInLootTables.ANCIENT_CITY)) {
                event.getTable().addPool(LootPool.lootPool().setRolls(UniformGenerator.between(1.0F, 2.0F))
                        .add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
                                "sword_soaring:sword_soaring"
                        )).when(LootItemRandomChanceCondition.randomChance(dropChanceModifier)))
                        .build());
            }

            if (event.getName().equals(BuiltInLootTables.ANCIENT_CITY_ICE_BOX)) {
                event.getTable().addPool(LootPool.lootPool().setRolls(UniformGenerator.between(1.0F, 2.0F))
                        .add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
                                "sword_soaring:sword_soaring"
                        ))).when(LootItemRandomChanceCondition.randomChance(dropChanceModifier))
                        .build());
            }

            if (event.getName().equals(BuiltInLootTables.END_CITY_TREASURE)) {
                event.getTable().addPool(LootPool.lootPool().setRolls(UniformGenerator.between(1.0F, 2.0F))
                        .add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
                                "sword_soaring:sword_soaring"
                        )).when(LootItemRandomChanceCondition.randomChance(dropChanceModifier)))
                        .build());
            }

            if (event.getName().equals(BuiltInLootTables.FISHING_TREASURE)) {
                event.getTable().addPool(LootPool.lootPool().setRolls(UniformGenerator.between(1.0F, 2.0F))
                        .add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
                                "sword_soaring:sword_soaring"
                        )).when(LootItemRandomChanceCondition.randomChance(dropChanceModifier)))
                        .build());
            }

            if (event.getName().equals(BuiltInLootTables.STRONGHOLD_LIBRARY)) {
                event.getTable().addPool(LootPool.lootPool().setRolls(UniformGenerator.between(1.0F, 5.0F))
                        .add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
                                "sword_soaring:sword_soaring"
                        ))).when(LootItemRandomChanceCondition.randomChance(dropChanceModifier * 0.3F))
                        .build());
            }

        }

    }

}
