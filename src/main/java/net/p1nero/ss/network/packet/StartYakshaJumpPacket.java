package net.p1nero.ss.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.p1nero.ss.capability.SSCapabilityProvider;
import net.p1nero.ss.network.PacketHandler;
import net.p1nero.ss.network.PacketRelay;
import yesman.epicfight.api.utils.LevelUtil;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import javax.annotation.Nullable;

/**
 * 实现跳跃的服务端操作
 */
public record StartYakshaJumpPacket(int tick) implements BasePacket {
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(tick);
    }

    public static StartYakshaJumpPacket decode(FriendlyByteBuf buf) {
        return new StartYakshaJumpPacket(buf.readInt());
    }

    @Override
    public void execute(@Nullable Player player) {
        if (player != null && player.getServer() != null) {
            player.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
                ssPlayer.setProtectNextFall(true);
            });
            player.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).ifPresent(entityPatch -> {
                if(entityPatch instanceof ServerPlayerPatch caster){
//                    caster.consumeStamina(2);
                    caster.playSound(EpicFightSounds.ROCKET_JUMP.get(), 1.0F, 0.0F, 0.0F);
                    caster.playSound(EpicFightSounds.ENTITY_MOVE.get(), 1.0F, 0.0F, 0.0F);
                    LevelUtil.circleSlamFracture(null, caster.getOriginal().level(), caster.getOriginal().position().subtract(0.0, 1.0, 0.0), (double)tick * 0.05, true, false, false);
                    Vec3 entityEyePos = caster.getOriginal().getEyePosition();
                    EpicFightParticles.AIR_BURST.get().spawnParticleWithArgument(caster.getOriginal().serverLevel(), entityEyePos.x, entityEyePos.y, entityEyePos.z, 0.0, 0.0, 2.0 + 0.05 * (double)tick);
                    caster.playAnimationSynchronized(Animations.BIPED_DEMOLITION_LEAP, 0.0F);
                }
            });
        }
    }
}