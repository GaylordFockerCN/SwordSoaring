package net.p1nero.ss.network.packet;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public interface BasePacket {
    void encode(FriendlyByteBuf var1);

    default boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            this.execute(context.get().getSender());
        });
        return true;
    }

    void execute(Player var1);
}
