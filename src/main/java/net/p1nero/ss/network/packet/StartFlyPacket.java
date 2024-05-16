package net.p1nero.ss.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.p1nero.ss.Config;
import net.p1nero.ss.SwordSoaring;
import net.p1nero.ss.capability.SSCapabilityProvider;
import net.p1nero.ss.entity.ModEntities;
import net.p1nero.ss.entity.SwordEntity;
import net.p1nero.ss.epicfight.animation.ModAnimations;
import net.p1nero.ss.network.PacketHandler;
import net.p1nero.ss.network.PacketRelay;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import javax.annotation.Nullable;

/**
 * 实现开始飞行的服务端操作
 */
public record StartFlyPacket () implements BasePacket {
    @Override
    public void encode(FriendlyByteBuf buf) {
    }

    public static StartFlyPacket decode(FriendlyByteBuf buf) {
        return new StartFlyPacket();
    }

    @Override
    public void execute(@Nullable Player player) {
        if (player != null && player.getServer() != null) {
            player.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
//                ssPlayer.setProtectNextFall(true);
                ssPlayer.setFlying(true);
                player.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).ifPresent(entityPatch -> {
                    if(entityPatch instanceof ServerPlayerPatch serverPlayerPatch){
                        DynamicAnimation dynamicAnimation = serverPlayerPatch.getServerAnimator().getPlayerFor(null).getAnimation();
                        System.out.println(dynamicAnimation.getId()+" "+ModAnimations.FLY_ON_SWORD_ADVANCED.getId());
                        if(!serverPlayerPatch.getEntityState().inaction() && !ssPlayer.isPlayingAnim){
                            serverPlayerPatch.playAnimationSynchronized(ModAnimations.FLY_ON_SWORD_ADVANCED, 0);
                        }
                    }
                });
            });
        }
    }
}