package net.p1nero.ss.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.p1nero.ss.Config;
import net.p1nero.ss.SwordSoaring;
import net.p1nero.ss.capability.SSCapabilityProvider;
import net.p1nero.ss.capability.SSPlayer;
import net.p1nero.ss.entity.SwordEntity;
import net.p1nero.ss.network.PacketHandler;
import net.p1nero.ss.network.PacketRelay;
import net.p1nero.ss.network.packet.AddSwordEntityPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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

    @Shadow @Final private Rarity rarity;

    /**
     * 御剑飞行的开关
     */
    @Inject(method = "use", at = @At("HEAD"))
    private void use(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir){
        if(SwordSoaring.epicFightLoad() && !Config.ENABLE_SPIRIT_FLY_IN_EFM.get()){
            return;
        }
        if(player.getAbilities().flying || (!player.getPersistentData().getBoolean("canFlySword") && !player.isCreative())){
            return;
        }
        ItemStack sword = player.getItemInHand(hand);
        if(!SwordSoaring.isValidSword(sword)){
            return;
        }

        if(level.isClientSide){
            return;
        }

        player.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
            ssPlayer.setFlying(!ssPlayer.isFlying());
            //重置初速度，防止太快起飞不了的bug。
            setFlySpeedScale(sword,0.7);
            //在use中只执行一次，不用判断是否已有sword entity
            if(ssPlayer.isFlying()){
                SwordEntity swordEntity = new SwordEntity(sword, player);
                swordEntity.setPos(player.getX(),player.getY(),player.getZ());
                swordEntity.setYRot(player.getYRot());
                player.level().addFreshEntity(swordEntity);
                PacketRelay.sendToAll(PacketHandler.INSTANCE, new AddSwordEntityPacket(player.getId()));
                if (player.level() instanceof ClientLevel clientLevel) {
                    clientLevel.putNonPlayerEntity(114514+player.getId(), swordEntity);
                }
                ssPlayer.putAwaySword(((ServerPlayer) player));
            }
            if(!ssPlayer.isFlying() && getLeftTick(sword) == 0){
                ssPlayer.setFlying(false);
                stopFly(sword);
                //还剑
                ssPlayer.returnSword(((ServerPlayer) player));

            }
        });

    }

    /**
     * w加速s减速在{@link ClientItemMixin}实现
     * 没有飞行的时候回复灵力
     */
    @Inject(method = "inventoryTick", at = @At("HEAD"))
    private void injected(ItemStack itemStack, Level level, Entity entity, int slotId, boolean isSelected, CallbackInfo ci){
        if(SwordSoaring.epicFightLoad() && !Config.ENABLE_SPIRIT_FLY_IN_EFM.get()){
            return;
        }
        if(SwordSoaring.isValidSword(itemStack) && (entity instanceof Player player)){
            if(player.getCapability(SSCapabilityProvider.SS_PLAYER).orElse(new SSPlayer()).isFlying()){
                //获取10tick前的速度并且根据按键对其缩放。
                double flySpeedScale = getFlySpeedScale(itemStack);
                Vec3 targetVec = getViewVec(itemStack, Config.INERTIA_TICK_BEFORE.get().intValue()).scale(flySpeedScale);
                if(targetVec.length() != 0){
                    //灵力够才能起飞
                    int spiritValue = getSpiritValue(itemStack) - (int) (targetVec.length() * 10);
                    if(spiritValue > 0){
                        if(!player.isCreative()){
                            setSpiritValue(itemStack, spiritValue);
                        }
                        //往朝向加速
                        if(Config.ENABLE_INERTIA.get()){
                            player.setDeltaMovement(targetVec);
                        }else {
                            player.setDeltaMovement(player.getViewVector(0).scale(Config.FLY_SPEED_SCALE.get().intValue()));
                        }
                    } else {
                        stopFly(itemStack);
                        player.getCapability(SSCapabilityProvider.SS_PLAYER).orElse(new SSPlayer()).returnSword(((ServerPlayer) player));
                    }
                }
            } else {
                //恢复灵力
                setSpiritValue(itemStack, getSpiritValue(itemStack) + 10);
                //缓冲
                if (getLeftTick(itemStack) > 0 && Config.ENABLE_INERTIA.get()) {
                    int leftTick = getLeftTick(itemStack);
                    setLeftTick(itemStack, leftTick - 1);
                    //用末速度来计算
                    double endVecLength = getEndVec(itemStack).length();
                    if (endVecLength != 0) {
                        double max = endVecLength * maxRecordTick * 2;
                        player.setDeltaMovement(getEndVec(itemStack).lerp(Vec3.ZERO, (max - leftTick) / max));
                    }
                }

            }

            //更新前几个刻的方向队列
            updateViewVec(itemStack, entity.getViewVector(0));
        }

    }

    @Inject(method = "appendHoverText", at = @At("HEAD"))
    private void injected(ItemStack itemStack, Level p_41422_, List<Component> components, TooltipFlag p_41424_, CallbackInfo ci){
        if(SwordSoaring.isValidSword(itemStack) && (!SwordSoaring.epicFightLoad() || Config.ENABLE_SPIRIT_FLY_IN_EFM.get())){
            components.add(Component.translatable("tip.sword_soaring.spirit_value", getSpiritValue(itemStack)));
        }
    }

}
