package net.p1nero.ss.network.packet;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.p1nero.ss.capability.SSCapabilityProvider;
import net.p1nero.ss.entity.SwordEntity;
import net.p1nero.ss.epicfight.SwordSoaringSkill;

import javax.annotation.Nullable;

import static net.p1nero.ss.util.ItemStackUtil.*;

/**
 * 实现飞行开关
 */
public record StartFlyPacket () implements BasePacket {
    @Override
    public void encode(FriendlyByteBuf buf) {
    }

    public static StartFlyPacket decode(FriendlyByteBuf buf) {
        return new StartFlyPacket();
    }

    @Override
    public void execute(@Nullable Player player) {
        if (player != null && player.getServer() != null) {
            player.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
                ssPlayer.setProtectNextFall(true);//重点是这个，其他没用
            });
        }
    }
}