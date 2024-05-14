package net.p1nero.ss.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.p1nero.ss.Config;
import net.p1nero.ss.capability.SSCapabilityProvider;

import javax.annotation.Nullable;

import static net.p1nero.ss.util.InertiaUtil.*;

/**
 * 实现飞行结束
 */
public record StopFlyPacket() implements BasePacket {
    @Override
    public void encode(FriendlyByteBuf buf) {
    }

    public static StopFlyPacket decode(FriendlyByteBuf buf) {
        return new StopFlyPacket();
    }

    @Override
    public void execute(@Nullable Player player) {
        if (player != null && player.getServer() != null) {
            player.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
                //飞行结束后再获取末向量。因为此时isFlying还没设为false
                if(Config.ENABLE_INERTIA.get() && ssPlayer.isFlying()){
                    //TODO 向下倾斜模拟重力
                    Vec3 endVec = getViewVec(player.getPersistentData(),1).scale(Config.FLY_SPEED_SCALE.get());
                    setEndVec(player.getPersistentData(), endVec);
                    double leftTick = endVec.length() * maxRecordTick;
                    setLeftTick(player.getPersistentData(), ((int) leftTick));
                }
                ssPlayer.setFlying(false);
                ssPlayer.returnSword(((ServerPlayer) player));
            });

        }
    }
}