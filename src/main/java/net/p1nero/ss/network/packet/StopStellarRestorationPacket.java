package net.p1nero.ss.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.p1nero.ss.capability.SSCapabilityProvider;

import javax.annotation.Nullable;

/**
 * 设置客户端滞空时间
 */
public record StopStellarRestorationPacket() implements BasePacket {
    @Override
    public void encode(FriendlyByteBuf buf) {
    }

    public static StopStellarRestorationPacket decode(FriendlyByteBuf buf) {
        return new StopStellarRestorationPacket();
    }

    @Override
    public void execute(@Nullable Player player) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if(localPlayer==null){
            return;
        }
        localPlayer.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
            ssPlayer.stayInAirTick = 20;
        });
    }
}