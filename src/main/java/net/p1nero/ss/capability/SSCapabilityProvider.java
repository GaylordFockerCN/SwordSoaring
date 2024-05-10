package net.p1nero.ss.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.p1nero.ss.SwordSoaring;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = SwordSoaring.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SSCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static Capability<SSPlayer> SS_PLAYER = CapabilityManager.get(new CapabilityToken<>() {});

    private SSPlayer ssPlayer = null;
    
    private final LazyOptional<SSPlayer> optional = LazyOptional.of(this::createSSPlayer);

    private SSPlayer createSSPlayer() {
        if(this.ssPlayer == null){
            this.ssPlayer = new SSPlayer();
        }

        return this.ssPlayer;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
        if(capability == SS_PLAYER){
            return optional.cast();
        }

        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        createSSPlayer().saveNBTData(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        createSSPlayer().loadNBTData(tag);
    }

    @Mod.EventBusSubscriber(modid = SwordSoaring.MOD_ID)
    public static class Registration {
        @SubscribeEvent
        public static void attachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof Player) {
               if(!event.getObject().getCapability(SSCapabilityProvider.SS_PLAYER).isPresent()){
                   event.addCapability(new ResourceLocation(SwordSoaring.MOD_ID, "ss_player"), new SSCapabilityProvider());
               }
            }
        }

        @SubscribeEvent
        public static void onPlayerCloned(PlayerEvent.Clone event) {
            if(event.isWasDeath()) {
                event.getOriginal().getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(oldStore -> {
                    event.getOriginal().getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(newStore -> {
                        newStore.copyFrom(oldStore);
                    });
                });
            }
        }

        @SubscribeEvent
        public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
            event.register(SSPlayer.class);
        }

    }


}
