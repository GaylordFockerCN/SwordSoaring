package net.p1nero.ss.epicfight;

import net.minecraft.client.Minecraft;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.p1nero.ss.capability.SSCapabilityProvider;
import net.p1nero.ss.network.PacketHandler;
import net.p1nero.ss.network.PacketRelay;
import net.p1nero.ss.network.packet.StartYakshaJumpPacket;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.gameasset.EpicFightSkills;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.passive.PassiveSkill;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

import java.util.UUID;

/**
 * 靖妖傩舞!
 * 本质是无CD的毁坏跳跃
 */
public class YakshaMask extends PassiveSkill {

    private static final UUID EVENT_UUID = UUID.fromString("051a9bb2-7541-11ee-b962-0242ac114517");

    //tick决定高度
    public static final int tick = 10;
    public YakshaMask(Builder<? extends Skill> builder) {
        super(builder);
    }

    @Override
    public void onInitiate(SkillContainer container) {
        PlayerEventListener listener = container.getExecuter().getEventListener();
        listener.addEventListener(PlayerEventListener.EventType.MOVEMENT_INPUT_EVENT, EVENT_UUID, (event) -> {
            LocalPlayerPatch localPlayerPatch = event.getPlayerPatch();
            Player player = localPlayerPatch.getOriginal();
            boolean jumpPressed = Minecraft.getInstance().options.keyJump.isDown();
            player.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {

                //player.onGround()此时已经是false，所以不能在这里判断
                if (!jumpPressed || player.getVehicle() != null || player.getAbilities().flying || !localPlayerPatch.isBattleMode()
                        || ssPlayer.getYakshaMaskTimer() == 0 || !ssPlayer.canYakshaMask) {
                    return;
                }
                //直接由这个来判断能否起跳，并且在tick事件中对其修改（已经懒得保存能否跳了）
                ssPlayer.canYakshaMask = false;

                PacketRelay.sendToServer(PacketHandler.INSTANCE, new StartYakshaJumpPacket(tick));
                int modifiedTicks = (int)(7.466800212860107 * Math.log10((float)tick + 1.0F) / Math.log10(2.0));
                Vec3f jumpDirection = new Vec3f(0.0F, (float)modifiedTicks * 0.05F, 0.0F);
                float xRot = Mth.clamp(70.0F + Mth.clamp(-60f, -90.0F, 0.0F), 0.0F, 70.0F);
                jumpDirection.add(0.0F, xRot / 70.0F * 0.05F, 0.0F);
                jumpDirection.rotate(xRot, Vec3f.X_AXIS);
                jumpDirection.rotate(-localPlayerPatch.getCameraYRot(), Vec3f.Y_AXIS);
                localPlayerPatch.getOriginal().setDeltaMovement(jumpDirection.toDoubleVector());
                System.out.println(jumpDirection.toDoubleVector());

            });

        });

        //监听技能，释放技能后进入夜叉傩面
        listener.addEventListener(PlayerEventListener.EventType.SKILL_EXECUTE_EVENT, EVENT_UUID, (event) -> {
            Skill skill = event.getSkillContainer().getSkill();
            Player player = event.getPlayerPatch().getOriginal();
            player.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
                //客户端找不到方法限制。。按G就可以启动。。能放技能的三把武器太那啥了
                if(skill.getCategory() == SkillCategories.WEAPON_INNATE){
                    ssPlayer.setYakshaMaskTimer(400);
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

    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);
        container.getExecuter().getEventListener().removeListener(PlayerEventListener.EventType.MOVEMENT_INPUT_EVENT, EVENT_UUID);
        container.getExecuter().getEventListener().removeListener(PlayerEventListener.EventType.HURT_EVENT_PRE, EVENT_UUID);
    }

    /**
     * 控制技能持续时间和冷却
     */
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        event.player.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
            int yakshaMaskTimer = ssPlayer.getYakshaMaskTimer();
            if(yakshaMaskTimer > 0){
                ssPlayer.setYakshaMaskTimer(yakshaMaskTimer-1);
            }
            if(event.player.onGround()){
                ssPlayer.canYakshaMask = true;
            }
        });
    }

    @Override
    public Skill getPriorSkill() {
        return EpicFightSkills.METEOR_STRIKE;
    }

}
