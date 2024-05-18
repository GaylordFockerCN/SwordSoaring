package net.p1nero.ss.network.packet.client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.p1nero.ss.entity.SwordEntity;
import net.p1nero.ss.network.packet.BasePacket;
import net.p1nero.ss.util.ClientHelper;

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

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ()-> ClientHelper.localPlayerDo((localPlayer)->{
            Player owner = ((Player) localPlayer.level().getEntity(ownerId));
            SwordEntity sword = ((SwordEntity) localPlayer.level().getEntity(swordId));
            if(owner == null || sword == null){
                return;
            }
            sword.setRider(owner);
        }));

    }
}