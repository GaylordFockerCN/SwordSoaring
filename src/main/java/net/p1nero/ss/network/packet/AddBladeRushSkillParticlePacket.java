package net.p1nero.ss.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.particle.EpicFightParticles;

import javax.annotation.Nullable;

/**
 * 添加命中粒子特效
 */
public record AddBladeRushSkillParticlePacket(Vec3 pos, Vec3 movement) implements BasePacket {
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(pos.x);
        buf.writeDouble(pos.y);
        buf.writeDouble(pos.z);
        buf.writeDouble(movement.x);
        buf.writeDouble(movement.y);
        buf.writeDouble(movement.z);
    }

    public static AddBladeRushSkillParticlePacket decode(FriendlyByteBuf buf) {
        return new AddBladeRushSkillParticlePacket(new Vec3(buf.readDouble(),buf.readDouble(),buf.readDouble()), new Vec3(buf.readDouble(),buf.readDouble(),buf.readDouble()));
    }

    @Override
    public void execute(@Nullable Player player) {

        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if(localPlayer != null){
            double d5 = movement.x;
            double d6 = movement.y;
            double d1 = movement.z;
            int i = 1;
            localPlayer.level().addParticle(EpicFightParticles.BLADE_RUSH_SKILL.get(), pos.x + d5 * (double)i / 4.0, pos.y + d6 * (double)i / 4.0, pos.z + d1 * (double)i / 4.0, -d5, -d6 + 0.2, -d1);
        }

    }
}