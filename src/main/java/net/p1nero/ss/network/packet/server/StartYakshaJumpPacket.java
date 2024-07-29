package net.p1nero.ss.network.packet.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.p1nero.ss.SwordSoaring;
import net.p1nero.ss.capability.SSCapabilityProvider;
import net.p1nero.ss.capability.SSPlayer;
import net.p1nero.ss.epicfight.animation.ModAnimations;
import net.p1nero.ss.epicfight.skill.ModSkills;
import net.p1nero.ss.network.packet.BasePacket;
import yesman.epicfight.api.utils.LevelUtil;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import javax.annotation.Nullable;

/**
 * 实现跳跃的服务端操作
 * 直接调用痛苦魔枪
 */
public record StartYakshaJumpPacket(int tick) implements BasePacket {
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(tick);
    }

    public static StartYakshaJumpPacket decode(FriendlyByteBuf buf) {
        return new StartYakshaJumpPacket(buf.readInt());
    }

    /**
     * 修改了 {@link yesman.epicfight.skill.mover.DemolitionLeapSkill#castSkill}
     */
    @Override
    public void execute(@Nullable Player player) {
        if (player != null && player.getServer() != null) {
            player.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).ifPresent(entityPatch -> {

                if(entityPatch instanceof ServerPlayerPatch caster){
                    SSPlayer ssPlayer = player.getCapability(SSCapabilityProvider.SS_PLAYER).orElse(new SSPlayer());
                    if(SwordSoaring.isWOMLoaded() && !(caster.hasStamina(6) || player.isCreative())){
                        ssPlayer.setProtectNextFall(true);
                        return;
                    }
                    ssPlayer.setProtectNextFall(true);
                    if(SwordSoaring.isWOMLoaded()){
                        ssPlayer.isYakshaFall = true;
                    }
                    caster.playSound(EpicFightSounds.ROCKET_JUMP.get(), 1.0F, 0.0F, 0.0F);
                    caster.playSound(EpicFightSounds.ENTITY_MOVE.get(), 1.0F, 0.0F, 0.0F);
                    LevelUtil.circleSlamFracture(null, caster.getOriginal().level(), caster.getOriginal().position().subtract(0.0, 1.0, 0.0), (double)tick * 5 * 0.05, true, false, false);
                    Vec3 entityEyePos = caster.getOriginal().getEyePosition();
                    EpicFightParticles.AIR_BURST.get().spawnParticleWithArgument(caster.getOriginal().serverLevel(), entityEyePos.x, entityEyePos.y, entityEyePos.z, 0.0, 0.0, 2.0 + 0.05 * (double)tick);

                    if(SwordSoaring.isWOMLoaded()){
                        if(!player.isCreative()){
                            caster.consumeForSkill(ModSkills.YAKSHA_MASK, Skill.Resource.STAMINA, 6);
                        }
                        caster.playAnimationSynchronized(ModAnimations.AGONY_PLUNGE_FORWARD, 0.0F);
                    }else {
                        caster.consumeForSkill(ModSkills.YAKSHA_MASK, Skill.Resource.STAMINA, 2);
                        caster.playAnimationSynchronized(Animations.BIPED_DEMOLITION_LEAP, 0.0F);
                    }

                }
            });
        }
    }
}