package net.p1nero.ss.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class ClientHelper {
    public static void getLocalPlayer(Player rider) {
        rider =  Minecraft.getInstance().player;
    }

    public static void localPlayerDo(Consumer<Player> consumer){
        if(Minecraft.getInstance().player != null && Minecraft.getInstance().level != null){
            consumer.accept(Minecraft.getInstance().player);
        }
    }

}
