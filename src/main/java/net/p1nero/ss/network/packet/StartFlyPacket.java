package net.p1nero.ss.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.p1nero.ss.SwordSoaring;
import net.p1nero.ss.capability.SSCapabilityProvider;
import net.p1nero.ss.entity.ModEntities;
import net.p1nero.ss.entity.SwordEntity;
import net.p1nero.ss.network.PacketHandler;
import net.p1nero.ss.network.PacketRelay;

import javax.annotation.Nullable;

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
                ssPlayer.setProtectNextFall(true);
//                ssPlayer.setFlying(!ssPlayer.isFlying());
                ssPlayer.setFlying(true);

                //下面注释掉的代码是尝试在服务端加剑的实体，实测速度会跟不上，于是改成向所有人发包。
                //让所有人都看到我的剑！
                PacketRelay.sendToServer(PacketHandler.INSTANCE, new AddSwordEntityPacket(player.getId()));

//                if(!ssPlayer.hasSwordEntity() && player instanceof ServerPlayer serverPlayer){
//                    SwordEntity swordEntity = ModEntities.SWORD.get().spawn(serverPlayer.serverLevel(), player.getOnPos(), MobSpawnType.MOB_SUMMONED);
//                    SwordSoaring.LOGGER.info("add sword entity "+ swordEntity.getId());
//                    swordEntity.setRider(player);
//                    swordEntity.setItemStack(player.getMainHandItem());
//                    swordEntity.setPos(player.getX(), player.getY(), player.getZ());
//                    swordEntity.setYRot(player.getYRot());
//                    PacketRelay.sendToPlayer(PacketHandler.INSTANCE, new AddSwordEntityPacket(swordEntity.getId()), serverPlayer);
//                    ssPlayer.setHasSwordEntity(true);
//                }
            });
        }
    }
}