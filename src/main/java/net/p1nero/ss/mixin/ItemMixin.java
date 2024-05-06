package net.p1nero.ss.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;
import net.p1nero.ss.SwordSoaring;
import net.p1nero.ss.capability.SSCapabilityProvider;
import net.p1nero.ss.capability.SSPlayer;
import net.p1nero.ss.entity.SwordEntity;
import net.p1nero.ss.network.PacketHandler;
import net.p1nero.ss.network.PacketRelay;
import net.p1nero.ss.network.packet.UpdateFlySpeedPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static net.p1nero.ss.util.ItemStackUtil.*;

/**
 * 实现对itemStack的控制。
 */
@Mixin(Item.class)
public class ItemMixin {

    /**
     * 御剑飞行的开关
     */
    @Inject(method = "use", at = @At("HEAD"))
    private void use(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir){
        if(ModList.get().isLoaded("epicfight")){
            return;
        }
        if(player.getAbilities().flying || (!player.getPersistentData().getBoolean("canFlySword") && !player.isCreative())){
            return;
        }
        ItemStack sword = player.getItemInHand(hand);
        if(!SwordSoaring.isValidSword(sword)){
            return;
        }

        player.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
            ssPlayer.setFlying(!ssPlayer.isFlying());
            //重置初速度，防止太快起飞不了的bug。
            setFlySpeedScale(sword,0.7);
            if(ssPlayer.isFlying()){
                SwordEntity swordEntity = new SwordEntity(sword, player);
                swordEntity.setPos(player.getX(),player.getY(),player.getZ());
                swordEntity.setYRot(player.getYRot());
                player.level().addFreshEntity(swordEntity);
                if(player.level() instanceof ClientLevel clientLevel){
                    clientLevel.putNonPlayerEntity(114514, swordEntity);
                }
            }
            if(!ssPlayer.isFlying() && getLeftTick(sword) == 0){
                ssPlayer.setFlying(false);
                stopFly(sword);
            }
        });

    }

    /**
     * w加速s减速
     * 没有飞行的时候回复灵力
     */
    @Inject(method = "inventoryTick", at = @At("HEAD"))
    private void injected(ItemStack itemStack, Level level, Entity entity, int slotId, boolean isSelected, CallbackInfo ci){
        if(ModList.get().isLoaded("epicfight")){
            return;
        }
        if(SwordSoaring.isValidSword(itemStack) && (entity instanceof Player player)){
            if(entity instanceof LocalPlayer localPlayer){
                double flySpeedScale = getFlySpeedScale(itemStack);
                if(localPlayer.input.up  && flySpeedScale < 1.5){
                    PacketRelay.sendToServer(PacketHandler.INSTANCE, new UpdateFlySpeedPacket(slotId, flySpeedScale+0.1));
                }
                if(localPlayer.input.down && flySpeedScale > 0.5){
                    PacketRelay.sendToServer(PacketHandler.INSTANCE, new UpdateFlySpeedPacket(slotId, flySpeedScale-0.1));
                }
            }
            if(!player.getCapability(SSCapabilityProvider.SS_PLAYER).orElse(new SSPlayer()).isFlying()){
                setSpiritValue(itemStack, getSpiritValue(itemStack) + 10);
            }

            //往朝向加速
            if(player.getCapability(SSCapabilityProvider.SS_PLAYER).orElse(new SSPlayer()).isFlying()){
                //获取10tick前的速度并且根据按键对其缩放。
                double flySpeedScale = getFlySpeedScale(itemStack);
                Vec3 targetVec = getViewVec(itemStack, 10).scale(flySpeedScale);
                if(targetVec.length() != 0){
                    //灵力够才能起飞
                    int spiritValue = getSpiritValue(itemStack) - (int) (targetVec.length() * 10);
                    if(spiritValue > 0){
//                    if(!entity.isCreative()){
                        setSpiritValue(itemStack, spiritValue);
//                    }
                        entity.setDeltaMovement(targetVec);
                    } else {
                        stopFly(itemStack);
                    }
                }
            } else {
                //缓冲
                if (getLeftTick(itemStack) > 0) {
                    int leftTick = getLeftTick(itemStack);
                    setLeftTick(itemStack, leftTick - 1);
                    //用末速度来计算
                    double endVecLength = getEndVec(itemStack).length();
                    if (endVecLength != 0) {
                        double max = endVecLength * maxRecordTick / 2;
                        entity.setDeltaMovement(getEndVec(itemStack).lerp(Vec3.ZERO, (max - leftTick) / max));
                    }
                }
            }

            //更新前几个刻的方向队列
            updateViewVec(itemStack, entity.getViewVector(0));
        }

    }

    @Inject(method = "appendHoverText", at = @At("HEAD"))
    private void injected(ItemStack itemStack, Level p_41422_, List<Component> components, TooltipFlag p_41424_, CallbackInfo ci){
        if(SwordSoaring.isValidSword(itemStack) && !ModList.get().isLoaded("epicfight")){
            components.add(Component.translatable("tip.sword_soaring.spirit_value", getSpiritValue(itemStack)));
        }
    }

}
