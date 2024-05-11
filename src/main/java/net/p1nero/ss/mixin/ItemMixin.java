package net.p1nero.ss.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.p1nero.ss.Config;
import net.p1nero.ss.SwordSoaring;
import net.p1nero.ss.enchantment.ModEnchantments;
import net.p1nero.ss.enchantment.SwordSoaringEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * 实现对itemStack的控制。
 */
@Mixin(Item.class)
public abstract class ItemMixin {

    /**
     * 添加附魔状态下耐力消耗说明
     */
    @Inject(method = "appendHoverText", at = @At("HEAD"))
    private void injected(ItemStack itemStack, Level p_41422_, List<Component> components, TooltipFlag p_41424_, CallbackInfo ci){
        if(SwordSoaring.isValidSword(itemStack) ){
            if(itemStack.getEnchantmentLevel(ModEnchantments.SWORD_SOARING.get()) > 0){
                float scale = SwordSoaringEnchantment.getScale(itemStack.getEnchantmentLevel(ModEnchantments.SWORD_SOARING.get()));
                components.add(Component.translatable("tip.sword_soaring.stamina_consume", Config.STAMINA_CONSUME_PER_TICK.get() + " × " + scale));
            }
        }

    }

}
