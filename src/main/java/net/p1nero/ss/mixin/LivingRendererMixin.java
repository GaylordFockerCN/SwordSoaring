package net.p1nero.ss.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.p1nero.ss.util.ItemStackUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.function.Consumer;

import static net.p1nero.ss.util.ItemStackUtil.*;

/**
 * 调整御剑飞行的时候的姿势
 * Adjusting the posture of the Sword Soaring
 */
@Mixin(LivingEntityRenderer.class)
public class LivingRendererMixin<T extends LivingEntity, M extends EntityModel<T>>
{
    private T entity;

    @Inject(method = "render*", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void capture(T entity, float entityYaw, float deltaTicks, PoseStack poseStack, MultiBufferSource source, int light, CallbackInfo ci, boolean shouldSit, float f, float f1, float f2, float f6, float f7, float f8, float f5)
    {
        this.entity = entity;
    }

    @Redirect(method = "render*", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"))
    public void changePose(M model, PoseStack poseStack, VertexConsumer consumer, int light, int overlay, float red, float green, float blue, float alpha)
    {
        if(entity instanceof Player player && !ModList.get().isLoaded("epicfight")){
            List<ItemStack> list = ItemStackUtil.searchSwordItem(player, (ItemStackUtil::isFlying));
            if(!list.isEmpty()){
                PlayerModel playerModel = ((PlayerModel) model);
                Consumer<ModelPart> setRot = (modelPart)->{
                    modelPart.xRot = 0;
                    modelPart.yRot = (float) Math.toRadians(-45);
                    modelPart.zRot = 0;
                };
                swordSoaring$handleModelPart(setRot, playerModel.body, playerModel.leftArm, playerModel.rightArm, playerModel.leftLeg, playerModel.rightLeg, playerModel.leftPants,playerModel.leftSleeve,playerModel.rightPants,playerModel.rightSleeve);
                Consumer<ModelPart> setZtoX = (modelPart)-> {
                    modelPart.z = (float) (modelPart.x * Math.sqrt(2) / 2);
                    modelPart.x = (float) (modelPart.x * Math.sqrt(2) / 2);
                };
                swordSoaring$handleModelPart(setZtoX, playerModel.leftArm, playerModel.rightArm, playerModel.leftSleeve, playerModel.rightSleeve);

                //FIXME 脚会有偏移，用上面那个会变单脚
                Consumer<ModelPart> setZtoX2 = (modelPart)-> {
                    modelPart.z = (float) (modelPart.x * Math.sqrt(2) / 2);
                };
                swordSoaring$handleModelPart(setZtoX2, playerModel.leftLeg, playerModel.rightLeg, playerModel.leftPants, playerModel.rightPants);


            }
        }
        model.renderToBuffer(poseStack, consumer, light, overlay, red, green, blue, alpha);
    }

    /**
     * 懒得操作统一写了个consumer批处理
     */
    @Unique
    private static void swordSoaring$handleModelPart(Consumer<ModelPart> consumer, ModelPart... modelParts){
        for(ModelPart modelPart : modelParts)
            consumer.accept(modelPart);
    }

}
