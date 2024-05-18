package net.p1nero.ss.network.packet.client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.p1nero.ss.Config;
import net.p1nero.ss.capability.SSCapabilityProvider;
import net.p1nero.ss.network.packet.BasePacket;
import net.p1nero.ss.util.ClientHelper;

import javax.annotation.Nullable;

/**
 * 设置客户端滞空时间
 */
public record SetClientYakshaMaskTimePacket() implements BasePacket {
    @Override
    public void encode(FriendlyByteBuf buf) {
    }

    public static SetClientYakshaMaskTimePacket decode(FriendlyByteBuf buf) {
        return new SetClientYakshaMaskTimePacket();
    }

    @Override
    public void execute(@Nullable Player player) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ()-> ClientHelper.localPlayerDo((localPlayer) ->
                localPlayer.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
                    ssPlayer.setYakshaMaskTimer(500);
                    ssPlayer.yakshaMaskCooldownTimer = Config.YAKSHAS_MASK_COOLDOWN.get().intValue();
                })));

    }
}