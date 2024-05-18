package net.p1nero.ss.network.packet.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.p1nero.ss.capability.SSCapabilityProvider;
import net.p1nero.ss.entity.SwordEntity;
import net.p1nero.ss.network.packet.BasePacket;

import javax.annotation.Nullable;

/**
 * 同步客户端的剑，让所有人都看到我的剑！但是还没测试过。.
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
        Player localPlayer = Minecraft.getInstance().player;
        if(localPlayer == null || Minecraft.getInstance().level == null){
            return;
        }
        Player owner = ((Player) localPlayer.level().getEntity(ownerId));
        if(owner == null){
            return;
        }
        owner.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
            if(!ssPlayer.hasSwordEntity()){
                SwordEntity swordEntity = new SwordEntity(owner.getMainHandItem(), owner);
                swordEntity.setPos(owner.getX(), owner.getY(), owner.getZ());
                swordEntity.setYRot(owner.getYRot());
                Minecraft.getInstance().level.putNonPlayerEntity(114514+ownerId, swordEntity);
                ssPlayer.setHasSwordEntity(true);
            }
        });
    }
}