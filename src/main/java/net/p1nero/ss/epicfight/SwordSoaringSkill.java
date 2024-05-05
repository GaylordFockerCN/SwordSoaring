package net.p1nero.ss.epicfight;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.p1nero.ss.capability.SSCapabilityProvider;
import net.p1nero.ss.entity.SwordEntity;
import net.p1nero.ss.network.PacketHandler;
import net.p1nero.ss.network.PacketRelay;
import net.p1nero.ss.network.packet.StartFlyPacket;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

import java.util.UUID;

public class SwordSoaringSkill extends Skill {

    private static final UUID EVENT_UUID = UUID.fromString("051a9bb2-7541-11ee-b962-0242ac114514");

    public SwordSoaringSkill(Builder<? extends Skill> builder) {
        super(builder);
    }

    @Override
    public void setParams(CompoundTag parameters) {
        super.setParams(parameters);
    }

    @Override
    public void onInitiate(SkillContainer container) {
        super.onInitiate(container);

        PlayerEventListener listener = container.getExecuter().getEventListener();

        listener.addEventListener(PlayerEventListener.EventType.MOVEMENT_INPUT_EVENT, EVENT_UUID, (event) -> {
            if (event.getPlayerPatch().getOriginal().getVehicle() != null || event.getPlayerPatch().getOriginal().getAbilities().flying || !event.getPlayerPatch().isBattleMode()
                    || event.getPlayerPatch().getEntityState().inaction()) {
                return;
            }

            // Check directly from the keybind because event.getMovementInput().isJumping doesn't allow to be set as true while player's jumping
            boolean jumpPressed = Minecraft.getInstance().options.keyJump.isDown();

            Player player = container.getExecuter().getOriginal();
            ItemStack sword = player.getMainHandItem();
            if (!(sword.getItem() instanceof SwordItem)) {
                return;
            }

            player.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
                if (jumpPressed && event.getPlayerPatch().getStamina() > 0.1f ) {
                    if(!ssPlayer.isCoolDown){
                        PacketRelay.sendToServer(PacketHandler.INSTANCE, new StartFlyPacket());
                        ssPlayer.setFlying(true);
                        ssPlayer.setProtectNextFall(true);
                        if(!ssPlayer.isHasEntity()){
                            SwordEntity swordEntity = new SwordEntity(sword, player);
                            swordEntity.setPos(player.getX(), player.getY(), player.getZ());
                            swordEntity.setYRot(player.getYRot());
                            swordEntity.setItemStack(sword);
                            swordEntity.setRider(player);
                            player.level().addFreshEntity(swordEntity);

                            //不这么加渲染不了，很奇怪
                            if (player.level() instanceof ClientLevel clientLevel) {
                                clientLevel.putNonPlayerEntity(114514, swordEntity);
                            }

                            ssPlayer.setHasEntity(true);
                        }
                        event.getPlayerPatch().setStamina(event.getPlayerPatch().getStamina() - 0.1f);
                        if(event.getPlayerPatch().getStamina() <= 0.1f){
                            ssPlayer.isCoolDown = true;
                        }
                    }else if(event.getPlayerPatch().getStamina() == event.getPlayerPatch().getMaxStamina()){
                        ssPlayer.isCoolDown = false;
                    }

                } else {
                    ssPlayer.setFlying(false);
                    event.getPlayerPatch().setStamina(event.getPlayerPatch().getStamina() + 0.05f);
                }
            });

        });

        listener.addEventListener(PlayerEventListener.EventType.HURT_EVENT_PRE, EVENT_UUID, (event) -> {
            if (event.getDamageSource().is(DamageTypeTags.IS_FALL) ) {
                Player player = event.getPlayerPatch().getOriginal();
                player.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
                    if(ssPlayer.isProtectNextFall()){
                        event.setAmount(0.0F);
                        event.setCanceled(true);
                        ssPlayer.setProtectNextFall(false);
                    }
                });
            }
        });

    }

    @Override
    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);

        PlayerEventListener listener = container.getExecuter().getEventListener();

        listener.removeListener(PlayerEventListener.EventType.MOVEMENT_INPUT_EVENT, EVENT_UUID);
        listener.removeListener(PlayerEventListener.EventType.HURT_EVENT_PRE, EVENT_UUID);

    }

}
