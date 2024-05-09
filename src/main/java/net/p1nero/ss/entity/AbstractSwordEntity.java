package net.p1nero.ss.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemStack;
import org.w3c.dom.Entity;

/**
 * 以后再优化一下，把几个剑共同的地方提出来
 */
public interface AbstractSwordEntity {
    public void setPose(PoseStack poseStack);
    public ItemStack getItemStack();
}
