package net.p1nero.ss.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class SwordSoaringSecret extends Item {
    public SwordSoaringSecret() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.getPersistentData().putBoolean("canFlySword", true);
        player.displayClientMessage(Component.translatable("tip.sword_soaring.learn"), true);
        return super.use(level, player, hand);
    }
}
