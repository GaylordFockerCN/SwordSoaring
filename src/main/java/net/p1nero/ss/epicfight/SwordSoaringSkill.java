package net.p1nero.ss.epicfight;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.Input;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.p1nero.ss.entity.SwordEntity;
import net.p1nero.ss.network.PacketHandler;
import net.p1nero.ss.network.PacketRelay;
import net.p1nero.ss.network.packet.StartFlyPacket;
import net.p1nero.ss.network.packet.UpdateFlySpeedPacket;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.events.engine.ControllEngine;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.network.server.SPSkillExecutionFeedback;
import yesman.epicfight.skill.ChargeableSkill;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

import java.util.UUID;

import static net.p1nero.ss.util.ItemStackUtil.*;

public class SwordSoaringSkill extends Skill implements ChargeableSkill {

    private static final UUID EVENT_UUID = UUID.fromString("051a9bb2-7541-11ee-b962-0242ac114514");

    public SwordSoaringSkill(Builder<? extends Skill> builder) {
        super(builder);
    }

    @Override
    public void onInitiate(SkillContainer container) {
        super.onInitiate(container);

        PlayerEventListener listener = container.getExecuter().getEventListener();

        listener.addEventListener(PlayerEventListener.EventType.MOVEMENT_INPUT_EVENT, EVENT_UUID, (event) -> {
            if (event.getPlayerPatch().getOriginal().getVehicle() != null || !event.getPlayerPatch().isBattleMode() || event.getPlayerPatch().getOriginal().getAbilities().flying
                     || event.getPlayerPatch().getEntityState().inaction()) {
                return;
            }

            // Check directly from the keybind because event.getMovementInput().isJumping doesn't allow to be set as true while player's jumping
            boolean jumpPressed = Minecraft.getInstance().options.keyJump.isDown();

            Player player = container.getExecuter().getOriginal();
            int tickInterval = player.getPersistentData().getInt("tickInterval");//防止连续按没用
            if (jumpPressed) {
                if(tickInterval < 0){
                    player.getPersistentData().putInt("tickInterval", 1);
                    tryFly(player);
                    PacketRelay.sendToServer(PacketHandler.INSTANCE, new StartFlyPacket());
                }else {
                    player.getPersistentData().putInt("tickInterval", tickInterval-1);
                }
            }

        });

        listener.addEventListener(PlayerEventListener.EventType.HURT_EVENT_PRE, EVENT_UUID, (event) -> {
            if (event.getDamageSource().is(DamageTypeTags.IS_FALL) ) { // This is not synced //TODO 记录本次是否减伤
                event.setAmount(0.0F);
                event.setCanceled(true);
            }
        });

    }

    public static void tryFly(Player player){
        if(player.getAbilities().flying){
            return;
        }
        ItemStack sword = player.getMainHandItem();
        if(!(sword.getItem() instanceof SwordItem)){
            return;
        }
        boolean isFlying = player.getPersistentData().getBoolean("isFlying");
        isFlying = !isFlying;
        player.getPersistentData().putBoolean("isFlying", isFlying);
        if(isFlying){
            SwordEntity swordEntity = new SwordEntity(sword, player);
            swordEntity.setPos(player.getX(),player.getY(),player.getZ());
            swordEntity.setItemStack(sword);
            swordEntity.setRider(player);
            player.level().addFreshEntity(swordEntity);
        }
        if(!isFlying && getLeftTick(sword) == 0){
            player.getPersistentData().putBoolean("isFlying", false);
        }
    }

    @Override
    public KeyMapping getKeyMapping() {
        return EpicFightKeyMappings.MOVER_SKILL;
    }

    @Override
    public void startCharging(PlayerPatch<?> playerPatch) {

    }

    @Override
    public void resetCharging(PlayerPatch<?> playerPatch) {

    }

    @Override
    public int getAllowedMaxChargingTicks() {
        return 80;
    }

    @Override
    public int getMaxChargingTicks() {
        return 40;
    }

    @Override
    public int getMinChargingTicks() {
        return 12;
    }

    @Override
    public void chargingTick(PlayerPatch<?> caster) {
        int chargingTicks = caster.getSkillChargingTicks();

        if (chargingTicks % 5 == 0 && caster.getAccumulatedChargeAmount() < this.getMaxChargingTicks()) {
            if (caster.consumeStamina(this.consumption)) {
                caster.setChargingAmount(caster.getChargingAmount() + 5);
            }
        }
    }

    @Override
    public void castSkill(ServerPlayerPatch serverPlayerPatch, SkillContainer skillContainer, int i, SPSkillExecutionFeedback spSkillExecutionFeedback, boolean b) {

    }

    @Override
    public void gatherChargingArguemtns(LocalPlayerPatch localPlayerPatch, ControllEngine controllEngine, FriendlyByteBuf friendlyByteBuf) {

    }

}
