package net.p1nero.ss.network.packet.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.p1nero.ss.capability.SSCapabilityProvider;
import net.p1nero.ss.epicfight.animation.ModAnimations;
import net.p1nero.ss.epicfight.skill.SwordSoaringSkill;
import net.p1nero.ss.network.packet.BasePacket;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import javax.annotation.Nullable;

/**
 * 实现开始飞行的服务端操作
 */
public record StartFlyPacket (float flySpeedLevel) implements BasePacket {
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(flySpeedLevel);
    }

    public static StartFlyPacket decode(FriendlyByteBuf buf) {
        return new StartFlyPacket(buf.readFloat());
    }

    @Override
    public void execute(@Nullable Player player) {
        SwordSoaringSkill.flySpeedLevel = flySpeedLevel;
        if (player != null && player.getServer() != null) {
            player.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
//                ssPlayer.setProtectNextFall(true);
                ssPlayer.setFlying(true);
                player.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).ifPresent(entityPatch -> {
                    if(entityPatch instanceof ServerPlayerPatch serverPlayerPatch){
                        if(!serverPlayerPatch.getEntityState().inaction() && !ssPlayer.isPlayingAnim){
                            if(flySpeedLevel == 1){
                                serverPlayerPatch.playAnimationSynchronized(ModAnimations.FLY_ON_SWORD_ADVANCED, 0.15F);
                            }else {
                                serverPlayerPatch.playAnimationSynchronized(ModAnimations.FLY_ON_SWORD_BASIC, 0.15F);
                            }
                        }
                    }
                });
            });
        }
    }
}