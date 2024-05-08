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
 * 同步客户端的剑
 */
public record SyncSwordOwnerPacket(int ownerId, int swordId) implements BasePacket {
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(ownerId);
        buf.writeInt(swordId);
    }

    public static SyncSwordOwnerPacket decode(FriendlyByteBuf buf) {
        return new SyncSwordOwnerPacket(buf.readInt(),buf.readInt());
    }

    @Override
    public void execute(@Nullable Player player) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if(localPlayer == null){
            return;
        }
        LocalPlayer owner = ((LocalPlayer) localPlayer.level().getEntity(ownerId));
        SwordEntity sword = ((SwordEntity) localPlayer.level().getEntity(swordId));
        if(owner == null || sword == null){
            return;
        }
        sword.setRider(owner);
        sword.setItemStack(owner.getMainHandItem());
    }
}