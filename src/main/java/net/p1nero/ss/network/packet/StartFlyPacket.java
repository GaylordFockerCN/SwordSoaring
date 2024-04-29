package net.p1nero.ss.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.p1nero.ss.entity.SwordEntity;

import javax.annotation.Nullable;

import static net.p1nero.ss.util.ItemStackUtil.*;

/**
 * 实现飞行开关
 */
public record StartFlyPacket (int slotID) implements BasePacket {
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.slotID());
    }

    public static StartFlyPacket decode(FriendlyByteBuf buf) {
        return new StartFlyPacket(buf.readInt());
    }

    @Override
    public void execute(@Nullable Player player) {
        if (player != null && player.getServer() != null) {
            ItemStack sword = player.getInventory().getItem(slotID);
            if(isFlying(sword)){
                return;
            }
            setFlying(sword, true);

            //重置初速度，防止太快起飞不了的bug
            setFlySpeedScale(sword,1);

//            if(!isFlying && getLeftTick(sword) == 0){
//                stopFly(sword);
//            }
            SwordEntity swordEntity = new SwordEntity(sword, player);
            swordEntity.setPos(player.getX(),player.getY(),player.getZ());
            player.level().addFreshEntity(swordEntity);
            player.startRiding(swordEntity);
        }
    }
}