package net.p1nero.ss.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.particle.EpicFightParticles;

import javax.annotation.Nullable;

/**
 * 妈的 record不能继承，直接复制了一份AddBladeRushSkillParticlePacket
 */
public record AddSmokeParticlePacket(Vec3 pos, Vec3 movement) implements BasePacket {
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(pos.x);
        buf.writeDouble(pos.y);
        buf.writeDouble(pos.z);
        buf.writeDouble(movement.x);
        buf.writeDouble(movement.y);
        buf.writeDouble(movement.z);
    }

    public static AddSmokeParticlePacket decode(FriendlyByteBuf buf) {
        return new AddSmokeParticlePacket(new Vec3(buf.readDouble(),buf.readDouble(),buf.readDouble()), new Vec3(buf.readDouble(),buf.readDouble(),buf.readDouble()));
    }

    @Override
    public void execute(@Nullable Player player) {

        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if(localPlayer != null){
            localPlayer.clientLevel.addParticle(ParticleTypes.SMOKE, pos.x , pos.y , pos.z , 0, 0.2, 0);
        }

    }
}