package net.p1nero.ss;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
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
import net.minecraftforge.registries.ForgeRegistries;
import net.p1nero.ss.capability.SSCapabilityProvider;
import net.p1nero.ss.enchantment.ModEnchantments;
import net.p1nero.ss.entity.ModEntities;
import net.p1nero.ss.entity.SwordEntityRenderer;
import net.p1nero.ss.epicfight.ModSkills;
import net.p1nero.ss.item.ModItems;
import net.p1nero.ss.network.PacketHandler;
import net.p1nero.ss.network.packet.StopFlyPacket;
import org.slf4j.Logger;
import yesman.epicfight.config.ConfigManager;
import yesman.epicfight.data.loot.function.SetSkillFunction;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.item.EpicFightItems;

import java.util.stream.Collectors;

import static net.p1nero.ss.util.ItemStackUtil.*;

@Mod(SwordSoaring.MOD_ID)
public class SwordSoaring {

    public static final String MOD_ID = "sword_soaring";
    public static final Logger LOGGER = LogUtils.getLogger();
    public SwordSoaring(){
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus fg_bus = MinecraftForge.EVENT_BUS;
        MinecraftForge.EVENT_BUS.register(this);
        ModEntities.ENTITIES.register(bus);
        ModItems.ITEMS.register(bus);
        ModEnchantments.ENCHANTMENTS.register(bus);
        bus.addListener(this::commonSetup);
        if(epicFightLoad()){
            fg_bus.addListener(ModSkills::BuildSkills);
        }

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        PacketHandler.register();
        if(SwordSoaring.epicFightLoad()){
            ModSkills.registerSkills();
        }
    }

    /**
     * 判断物品是否属于剑或者被视为剑。
     * 无法监听事件，干脆直接在这里初始化剑物品表。
     */
    public static boolean isValidSword(ItemStack sword){
        //不知为何无法监听
        if(Config.swordItems.isEmpty()){
            Config.swordItems = Config.ITEM_STRINGS.get().stream()
                    .map(itemName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName)))
                    .collect(Collectors.toSet());
        }
        return sword.getItem() instanceof SwordItem  || Config.swordItems.contains(sword.getItem());
    }

    public static boolean epicFightLoad(){
        return ModList.get().isLoaded("epicfight");
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents{
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event){
            EntityRenderers.register(ModEntities.SWORD.get(), SwordEntityRenderer::new);
            EntityRenderers.register(ModEntities.RAIN_SCREEN_SWORD.get(), SwordEntityRenderer::new);
        }

    }

    @Mod.EventBusSubscriber(modid = MOD_ID)
    public static class ModEvents{

        /**
         * 控制飞行和耐力消耗
         * 并进行惯性判断。飞行结束时如果有缓冲时间则缓冲。
         * 缓冲时间设置请看：{@link StopFlyPacket#execute(Player)}
         */
        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            Player player = event.player;
            if(!SwordSoaring.epicFightLoad()){
                return;
            }

            player.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
                if(ssPlayer.isFlying()){
                    //惯性控制。懒得重写就直接用getPersistentData了
                    if(Config.ENABLE_INERTIA.get()){
                        Vec3 targetVec = getViewVec(player.getPersistentData(), Config.INERTIA_TICK_BEFORE.get().intValue()).scale(Config.FLY_SPEED_SCALE.get());
                        if(targetVec.length() != 0) {
                            player.setDeltaMovement(targetVec);
                        }
                    } else {
                        player.setDeltaMovement(player.getViewVector(0.5f).scale(Config.FLY_SPEED_SCALE.get()));
                    }

                    //消耗耐力
                    player.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).ifPresent((entityPatch)->{
                        if(entityPatch instanceof PlayerPatch<?> playerPatch){
                            if(!player.isCreative()){
                                float scale = 1;
                                if(ssPlayer.getSword() != null){
                                    int enchantmentLevel = ssPlayer.getSword().getEnchantmentLevel(ModEnchantments.SWORD_SOARING.get());
                                    scale = switch (enchantmentLevel) {
                                        case 1 -> 0.75f;
                                        case 2 -> 0.5f;
                                        default -> 1;
                                    };
                                }
                                playerPatch.consumeStamina(Config.STAMINA_CONSUME_PER_TICK.get().floatValue() * scale);
                            }
                        }
                    });
                } else if(Config.ENABLE_INERTIA.get()){
                    double endVecLength = getEndVec(player.getPersistentData()).length();
                    //惯性缓冲
                    if (getLeftTick(player.getPersistentData()) > 0 && endVecLength != 0) {
                        int leftTick = getLeftTick(player.getPersistentData());
                        setLeftTick(player.getPersistentData(), leftTick - 1);
                        //用末速度来计算
                        double max = endVecLength * maxRecordTick;
                        player.setDeltaMovement(getEndVec(player.getPersistentData()).lerp(Vec3.ZERO, (max - leftTick) / max));
                    }
                }
            });

            //更新方向向量队列
            updateViewVec(player.getPersistentData(), player.getViewVector(0));

        }

        /**
         * 把技能书加到箱子里
         */
        @SubscribeEvent
        public static void modifyVanillaLootPools(final LootTableLoadEvent event) {
            if(!SwordSoaring.epicFightLoad()){
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
