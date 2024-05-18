package net.p1nero.ss.epicfight.skill;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.p1nero.ss.Config;
import net.p1nero.ss.SwordSoaring;
import net.p1nero.ss.capability.SSCapabilityProvider;
import net.p1nero.ss.capability.SSPlayer;
import net.p1nero.ss.entity.StellarSwordEntity;
import net.p1nero.ss.epicfight.animation.ModAnimations;
import net.p1nero.ss.network.PacketHandler;
import net.p1nero.ss.network.PacketRelay;
import net.p1nero.ss.network.packet.server.StartStellarRestorationPacket;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 星斗归位！
 */
public class StellarRestoration extends Skill {

    private static final UUID EVENT_UUID = UUID.fromString("051a9bb2-7541-11ee-b962-0242ac114518");
    public StellarRestoration(Builder<? extends Skill> builder) {
        super(builder);
    }

    @Override
    public void onInitiate(SkillContainer container) {
        super.onInitiate(container);
        PlayerEventListener listener = container.getExecuter().getEventListener();

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

    /**
     * 实现滞空和蓄力判断
     * 蓄力可以判断第几次按下，根据按下的奇偶次不同从而实现发不同的包
     */
    public static void onPlayerTick(TickEvent.PlayerTickEvent event){
        Player player = event.player;
        player.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {

            if(ssPlayer.stellarRestorationCooldownTimer > 0){
                ssPlayer.stellarRestorationCooldownTimer--;
            }

            //滞空
            if(ssPlayer.stayInAirTick > 0){
                player.setDeltaMovement(player.getDeltaMovement().scale(0.01));
                ssPlayer.stayInAirTick--;
            }

            //蓄力判断
            //isStellarRestorationSecondPressing 可以理解为用来判断是否是第二次按下
            // isStellarRestorationPressing 用来判断是否按下
            if(!player.isLocalPlayer()){//该死的服务端
                return;
            }
            player.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).ifPresent(entityPatch -> {
                if(entityPatch instanceof LocalPlayerPatch caster){

                    if(caster.getSkill(ModSkills.STELLAR_RESTORATION) == null || ssPlayer.stellarRestorationCooldownTimer > 0 || !SwordSoaring.isValidSword(player.getMainHandItem())){
                        return;
                    }

                    boolean isDodgeKeyPress = EpicFightKeyMappings.DODGE.isDown();

                    //奇数次按就播放动画。需要一直播，防止中断
                    if(isDodgeKeyPress && !ssPlayer.isStellarRestorationSecondPressing){
                        caster.playAnimationSynchronized(ModAnimations.STELLAR_RESTORATION_PRE0, 0.0F);
                        player.setYBodyRot(player.getYHeadRot());//不这样的话头动身不动
                        player.setDeltaMovement(Vec3.ZERO);
                    }

                    //isStellarRestorationReady表示第二次按。如果是第二次按就清空状态。如果是第一次按下那就先标记为true，为上面那个铺垫
                    if(isDodgeKeyPress){
                        ssPlayer.isStellarRestorationPressing = true;
                        return;
                    }

                    //如果isStellarRestoration是true而且没按下，说明是按下后松开了
                    if(ssPlayer.isStellarRestorationPressing){
//                    System.out.println("Released");
                        //第二次按下松开后的处理
                        if(ssPlayer.isStellarRestorationSecondPressing){
                            ssPlayer.isStellarRestorationPressing = false;
                            ssPlayer.isStellarRestorationSecondPressing = false;
                            ssPlayer.stellarRestorationCooldownTimer = Config.STELLAR_RESTORATION_COOLDOWN.get().intValue();
                            PacketRelay.sendToServer(PacketHandler.INSTANCE, new StartStellarRestorationPacket(true));
                            ssPlayer.stayInAirTick = 20;//滞空处理
                            return;
                        }

                        PacketRelay.sendToServer(PacketHandler.INSTANCE, new StartStellarRestorationPacket(false));
                        //客户端请求服务端执行技能的最好方法
                        caster.getSkill(ModSkills.STELLAR_RESTORATION).sendExecuteRequest(caster, ClientEngine.getInstance().controllEngine);

                        ssPlayer.isStellarRestorationPressing = false;
                        ssPlayer.isStellarRestorationSecondPressing = true;
                    }
                }
            });

        });
    }

    public static void summonSword(ServerPlayer player, SSPlayer ssPlayer){
        if(!SwordSoaring.isValidSword(player.getMainHandItem())){
            return;
        }
        StellarSwordEntity sword = new StellarSwordEntity(player.getMainHandItem(), player.level());
        ssPlayer.stellarSwordID = sword.getId();
        sword.setOwner(player);
        sword.setNoGravity(true);
        sword.setBaseDamage(0.01);
        sword.setSilent(false);
        sword.pickup = AbstractArrow.Pickup.DISALLOWED;
        sword.setKnockback(0);//击退
        sword.setPierceLevel((byte) 5);//穿透
        sword.setPos(player.getEyePosition(1).add(0,1,0));
        Vec3 view = player.getViewVector(1f);
        sword.shoot(view.x, view.y, view.z, 3,0);//后两个是速度和偏移
        player.level().playSound(null, sword.getOnPos(), EpicFightSounds.ENTITY_MOVE.get(), SoundSource.BLOCKS, 1,1);
        player.serverLevel().addFreshEntity(sword);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean shouldDraw(SkillContainer container) {
        AtomicBoolean toReturn = new AtomicBoolean(false);
        container.getExecuter().getOriginal().getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
            toReturn.set(ssPlayer.stellarRestorationCooldownTimer > 0);
        });
        return toReturn.get();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y) {
        Player player = container.getExecuter().getOriginal();
        SSPlayer ssPlayer = player.getCapability(SSCapabilityProvider.SS_PLAYER).orElse(new SSPlayer());
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(0, (float)gui.getSlidingProgression(), 0);
        guiGraphics.blit(ModSkills.STELLAR_RESTORATION.getSkillTexture(), (int)x, (int)y, 24, 24, 0, 0, 1, 1, 1, 1);
        guiGraphics.drawString(gui.font, String.format("%.1f", (ssPlayer.stellarRestorationCooldownTimer / 40.0f)), x + 3, y + 6, 16777215, true);
        poseStack.popPose();
    }

}
