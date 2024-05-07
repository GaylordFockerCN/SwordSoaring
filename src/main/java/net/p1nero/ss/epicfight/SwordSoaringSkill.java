package net.p1nero.ss.epicfight;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.Vec3;
import net.p1nero.ss.Config;
import net.p1nero.ss.SwordSoaring;
import net.p1nero.ss.capability.SSCapabilityProvider;
import net.p1nero.ss.entity.SwordEntity;
import net.p1nero.ss.network.PacketHandler;
import net.p1nero.ss.network.PacketRelay;
import net.p1nero.ss.network.packet.AddSwordEntityPacket;
import net.p1nero.ss.network.packet.StartFlyPacket;
import net.p1nero.ss.network.packet.StopFlyPacket;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

import java.util.UUID;

import static net.p1nero.ss.util.ItemStackUtil.*;
import static net.p1nero.ss.util.ItemStackUtil.setLeftTick;

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


            // Check directly from the keybind because event.getMovementInput().isJumping doesn't allow to be set as true while player's jumping
            boolean jumpPressed = Minecraft.getInstance().options.keyJump.isDown();

            Player player = container.getExecuter().getOriginal();
            ItemStack sword = player.getMainHandItem();

            player.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {

                //最后一个条件是防止飞行的时候切物品会导致永久飞行不掉落。必须是剑或者被视为剑的物品才可以“御”
                if (!jumpPressed || event.getPlayerPatch().getOriginal().getVehicle() != null || event.getPlayerPatch().getOriginal().getAbilities().flying || !event.getPlayerPatch().isBattleMode()
                        || event.getPlayerPatch().getStamina() <= 0.1f || !(SwordSoaring.isValidSword(sword) || ssPlayer.hasSwordEntity()) ) {
                    //停止飞行
                    PacketRelay.sendToServer(PacketHandler.INSTANCE, new StopFlyPacket());
                    //飞行结束后再获取末向量。因为此时isFlying还没设为false
                    if(Config.ENABLE_INERTIA.get() && ssPlayer.isFlying()){
                        Vec3 endVec = getViewVec(player.getPersistentData(),1).scale(Config.FLY_SPEED_SCALE.get());
                        setEndVec(player.getPersistentData(), endVec);
                        double leftTick = endVec.length() * maxRecordTick / 2;
                        setLeftTick(player.getPersistentData(), ((int) leftTick));
                    }
                    ssPlayer.setFlying(false);
                    //重置飞行前摇时间
                    ssPlayer.setAnticipationTick(0);
                    return;
                }

                //进行前摇判断，按住空格0.5s后才起飞（不然就跳不了了..）
                if(ssPlayer.getAnticipationTick() == 0){
                    ssPlayer.setAnticipationTick(Config.MAX_ANTICIPATION_TICK.get().intValue());
                    return;
                }
                if(ssPlayer.getAnticipationTick() > 1){
                    ssPlayer.setAnticipationTick(ssPlayer.getAnticipationTick()-1);
                    return;
                }

                //设置飞行状态并设置免疫下次摔落伤害
                PacketRelay.sendToServer(PacketHandler.INSTANCE, new StartFlyPacket());
                ssPlayer.setFlying(true);

                //向世界添加剑的实体
                if(!ssPlayer.hasSwordEntity()){
                    SwordEntity swordEntity = new SwordEntity(sword, player);
                    swordEntity.setPos(player.getX(), player.getY(), player.getZ());
                    swordEntity.setYRot(player.getYRot());
                    //服务端加的话移动跟不上，所以在客户端加就好
                    if (player.level() instanceof ClientLevel clientLevel) {
                        clientLevel.putNonPlayerEntity(114514 + player.getId(), swordEntity);
                    }
                    ssPlayer.setHasSwordEntity(true);
                }

            });

        });

        //免疫摔落伤害
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
