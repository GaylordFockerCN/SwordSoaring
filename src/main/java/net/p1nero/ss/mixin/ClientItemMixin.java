package net.p1nero.ss.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.p1nero.ss.Config;
import net.p1nero.ss.SwordSoaring;
import net.p1nero.ss.capability.SSCapabilityProvider;
import net.p1nero.ss.entity.SwordEntity;
import net.p1nero.ss.network.PacketHandler;
import net.p1nero.ss.network.PacketRelay;
import net.p1nero.ss.network.packet.UpdateFlySpeedPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.p1nero.ss.util.ItemStackUtil.*;

/**
 * 开发环境runServer会出错。。。。
 */
@Mixin(Item.class)
public class ClientItemMixin {

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

        if(!level.isClientSide){
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
            if(!ssPlayer.isFlying() && getLeftTick(sword.getOrCreateTag()) == 0){
                ssPlayer.setFlying(false);
                stopFly(sword);
            }
        });

    }

    /**
     * w加速s减速
     */
    @Inject(method = "inventoryTick", at = @At("HEAD"))
    private void injected(ItemStack itemStack, Level level, Entity entity, int slotId, boolean isSelected, CallbackInfo ci){
        if(SwordSoaring.epicFightLoad() && !Config.ENABLE_SPIRIT_FLY_IN_EFM.get()){
            return;
        }
        if(SwordSoaring.isValidSword(itemStack) && (entity instanceof Player player)){
            if(player instanceof LocalPlayer localPlayer){
                double flySpeedScale = getFlySpeedScale(itemStack);
                if(localPlayer.input.up  && flySpeedScale < 1.5){
                    PacketRelay.sendToServer(PacketHandler.INSTANCE, new UpdateFlySpeedPacket(slotId, flySpeedScale+0.1));
                }
                if(localPlayer.input.down && flySpeedScale > 0.5){
                    PacketRelay.sendToServer(PacketHandler.INSTANCE, new UpdateFlySpeedPacket(slotId, flySpeedScale-0.1));
                }
            }
        }

    }
}
