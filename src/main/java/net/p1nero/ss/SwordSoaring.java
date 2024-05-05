package net.p1nero.ss;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
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
import net.p1nero.ss.entity.ModEntities;
import net.p1nero.ss.entity.SwordEntityRenderer;
import net.p1nero.ss.epicfight.ModSkills;
import net.p1nero.ss.item.ModItems;
import net.p1nero.ss.network.PacketHandler;
import net.p1nero.ss.network.PacketRelay;
import net.p1nero.ss.network.packet.UpdateFlySpeedPacket;
import net.p1nero.ss.util.ItemStackUtil;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import static net.p1nero.ss.util.ItemStackUtil.*;

@Mod(SwordSoaring.MOD_ID)
public class SwordSoaring {

    public static final String MOD_ID = "sword_soaring";

    public SwordSoaring(){
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        ModEntities.REGISTRY.register(bus);
        ModItems.REGISTRY.register(bus);
        bus.addListener(this::commonSetup);
//        if(ModList.get().isLoaded("epicfight")){
//            bus.addListener(ModSkills::BuildSkills);
//        }

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
        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            Player player = event.player;
            player.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
                if(ssPlayer.isFlying()){
                    player.setDeltaMovement(player.getViewVector(0.5f).scale(0.6));
                }
            });
        }
    }

}
