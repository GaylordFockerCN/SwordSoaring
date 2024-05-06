package net.p1nero.ss.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.fml.ModList;
import net.p1nero.ss.SwordSoaring;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.world.item.LongswordItem;
import yesman.epicfight.world.item.TachiItem;
import yesman.epicfight.world.item.UchigatanaItem;

public class SwordEntityRenderer extends EntityRenderer<SwordEntity> {

    public SwordEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    /**
     * 调用物品渲染方法，渲染实体绑定的物品
     * 调整姿势调了好久awa
     */
    @Override
    public void render(SwordEntity swordEntity, float p_114486_, float p_114487_, PoseStack poseStack, @NotNull MultiBufferSource multiBufferSource, int light) {
        poseStack.pushPose();
        if(SwordSoaring.epicFightLoad()){
            Item sword = swordEntity.getItemStack().getItem();
            if(sword instanceof UchigatanaItem || sword instanceof TachiItem || sword instanceof LongswordItem){
                poseStack.mulPose(Axis.XP.rotationDegrees(90f));
                poseStack.mulPose(Axis.ZP.rotationDegrees(45f + swordEntity.getYRot()));
            }
        }else {
            poseStack.mulPose(Axis.XP.rotationDegrees(90f));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-45f + swordEntity.getYRot()));
        }
//        poseStack.translate(0,0,0);
        BakedModel model = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(swordEntity.getItemStack());
        Minecraft.getInstance().getItemRenderer().render(swordEntity.getItemStack(), ItemDisplayContext.FIXED,false,poseStack,multiBufferSource, light,1, model);
        poseStack.popPose();
        super.render(swordEntity, p_114486_, p_114487_, poseStack, multiBufferSource, light);
    }

    @Override
    public ResourceLocation getTextureLocation(SwordEntity swordEntity) {
        return TextureMapping.getItemTexture(swordEntity.getItemStack().getItem());
    }
}
