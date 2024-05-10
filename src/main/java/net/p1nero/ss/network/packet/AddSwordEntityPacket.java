package net.p1nero.ss.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.p1nero.ss.capability.SSCapabilityProvider;
import net.p1nero.ss.entity.SwordEntity;

import javax.annotation.Nullable;

/**
 * 同步客户端的剑，让所有人都看到我的剑！但是还没测试过。
 */
public record AddSwordEntityPacket(int ownerId) implements BasePacket {
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(ownerId);
    }

    public static AddSwordEntityPacket decode(FriendlyByteBuf buf) {
        return new AddSwordEntityPacket(buf.readInt());
    }

    @Override
    public void execute(@Nullable Player player) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        LocalPlayer owner = ((LocalPlayer) localPlayer.level().getEntity(ownerId));
        owner.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
            if(!ssPlayer.hasSwordEntity()){
                SwordEntity swordEntity = new SwordEntity(owner.getMainHandItem(), owner);
                swordEntity.setPos(player.getX(), player.getY(), player.getZ());
                swordEntity.setYRot(player.getYRot());
                //服务端加的话移动跟不上，所以在客户端加就好
                if (player.level() instanceof ClientLevel clientLevel) {
                    clientLevel.putNonPlayerEntity(114514+ownerId, swordEntity);
                }
                ssPlayer.setHasSwordEntity(true);
            }
        });
    }
}