package net.p1nero.ss.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.p1nero.ss.capability.SSCapabilityProvider;
import net.p1nero.ss.capability.SSPlayer;
import net.p1nero.ss.epicfight.animation.ModAnimations;
import net.p1nero.ss.epicfight.skill.ModSkills;
import net.p1nero.ss.network.PacketHandler;
import net.p1nero.ss.network.PacketRelay;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import javax.annotation.Nullable;

import static net.p1nero.ss.epicfight.skill.StellarRestoration.summonSword;

/**
 * 告诉服务端我正在蓄力
 * true为播动画，false为发射
 */
public record StartStellarRestorationPacket(boolean end) implements BasePacket {
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(end);
    }

    public static StartStellarRestorationPacket decode(FriendlyByteBuf buf) {
        return new StartStellarRestorationPacket(buf.readBoolean());
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
                        if(end){
                            //瞬移到剑的位置
                            Entity sword = player.level().getEntity(ssPlayer.stellarSwordID);
                            if(sword != null){
                                player.teleportTo(sword.getX(), sword.getY(), sword.getZ());
                                sword.discard();
                                caster.playAnimationSynchronized(Animations.SWEEPING_EDGE,0);
                                ssPlayer.stayInAirTick = 20;
                                if(player instanceof ServerPlayer serverPlayer){
                                    PacketRelay.sendToPlayer(PacketHandler.INSTANCE, new StopStellarRestorationPacket(),serverPlayer);
                                }
                            }
                        } else {
                            //发射出剑
                            if(player instanceof ServerPlayer serverPlayer && caster.hasStamina(4)){
                                if(!player.isCreative()){
                                    caster.consumeStamina(5);
                                }
                                summonSword(serverPlayer, ssPlayer);
                                caster.playAnimationSynchronized(ModAnimations.STELLAR_RESTORATION_PRE,0);
                                ssPlayer.isStellarRestorationPressing = true;
                                ssPlayer.setProtectNextFall(true);
                            }
                        }
                    }
                }
            });
        }
    }
}