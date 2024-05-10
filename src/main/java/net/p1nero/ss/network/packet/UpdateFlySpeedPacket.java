package net.p1nero.ss.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * 更新速度，仅限无史诗战斗
 */
public record UpdateFlySpeedPacket (int slotID, double newSpeed) implements BasePacket {
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.slotID());
        buf.writeDouble(this.newSpeed());
    }

    public static UpdateFlySpeedPacket decode(FriendlyByteBuf buf) {
        return new UpdateFlySpeedPacket(buf.readInt(), buf.readDouble());
    }

    @Override
    public void execute(@Nullable Player playerEntity) {
        if (playerEntity != null && playerEntity.getServer() != null) {
            ItemStack sword = playerEntity.getInventory().getItem(slotID);
            sword.getOrCreateTag().putDouble("flySpeedScale", newSpeed);
        }
    }
}