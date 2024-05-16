package net.p1nero.ss.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.p1nero.ss.capability.SSCapabilityProvider;
import net.p1nero.ss.capability.SSPlayer;
import net.p1nero.ss.epicfight.animation.ModAnimations;
import net.p1nero.ss.epicfight.skill.ModSkills;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import javax.annotation.Nullable;

/**
 * 告诉服务端我正在蓄力
 * true为播动画，false为发射
 */
public record StartPreStellarRestorationPacket(boolean shouldPlayAnim) implements BasePacket {
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(shouldPlayAnim);
    }

    public static StartPreStellarRestorationPacket decode(FriendlyByteBuf buf) {
        return new StartPreStellarRestorationPacket(buf.readBoolean());
    }

    @Override
    public void execute(@Nullable Player player) {
        if (player != null && player.getServer() != null) {
            player.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).ifPresent(entityPatch -> {

                if(entityPatch instanceof ServerPlayerPatch caster){
                    if(!caster.hasStamina(5)){
                        return;
                    }
                    if(!caster.getSkill(ModSkills.STELLAR_RESTORATION).isEmpty()){
                        SSPlayer ssPlayer = player.getCapability(SSCapabilityProvider.SS_PLAYER).orElse(new SSPlayer());
                        if(shouldPlayAnim){
                            caster.playAnimationSynchronized(ModAnimations.STELLAR_RESTORATION_PRE0, 0.0F);
                        } else {
                            ssPlayer.isStellarRestorationReady = true;
                        }
                    }
                }
            });
        }
    }
}