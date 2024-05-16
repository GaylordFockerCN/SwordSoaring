package net.p1nero.ss.epicfight.skill;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.p1nero.ss.SwordSoaring;
import net.p1nero.ss.capability.SSCapabilityProvider;
import net.p1nero.ss.capability.SSPlayer;
import net.p1nero.ss.entity.StellarSwordEntity;
import net.p1nero.ss.epicfight.animation.ModAnimations;
import net.p1nero.ss.network.PacketHandler;
import net.p1nero.ss.network.PacketRelay;
import net.p1nero.ss.network.packet.StartPreStellarRestorationPacket;
import net.p1nero.ss.network.packet.StopStellarRestorationPacket;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.input.CombatKeyMapping;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

import java.util.UUID;

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

        listener.addEventListener(PlayerEventListener.EventType.SKILL_EXECUTE_EVENT, EVENT_UUID, (event) -> {
            if(event.getSkillContainer().getSkill() instanceof StellarRestoration){
                event.getPlayerPatch().getOriginal().getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
                    Player player = event.getPlayerPatch().getOriginal();

                    if(ssPlayer.isStellarRestorationReady && !ssPlayer.isStellarRestoration){
                        if(player instanceof ServerPlayer serverPlayer && event.getPlayerPatch().hasStamina(4)){
                            if(!player.isCreative()){
                                event.getPlayerPatch().consumeStamina(5);
                            }
                            summonSword(serverPlayer, ssPlayer);
                            event.getPlayerPatch().playAnimationSynchronized(ModAnimations.STELLAR_RESTORATION_PRE,0);
                            ssPlayer.isStellarRestoration = true;
                            ssPlayer.setProtectNextFall(true);
                        }
                    }else if(ssPlayer.isStellarRestoration){
                        if(player.isLocalPlayer()){
                            return;
                        }
                        Entity sword = player.level().getEntity(ssPlayer.stellarSwordID);
                        if(sword != null){
                            player.teleportTo(sword.getX(), sword.getY(), sword.getZ());
                            sword.discard();
                            event.getPlayerPatch().playAnimationSynchronized(Animations.SWEEPING_EDGE,0);
                            ssPlayer.stayInAirTick = 20;
                            if(player instanceof ServerPlayer serverPlayer){
                                PacketRelay.sendToPlayer(PacketHandler.INSTANCE, new StopStellarRestorationPacket(),serverPlayer);
                            }
                        }
                        //不管能不能瞬移都要重置
                        ssPlayer.isStellarRestoration = false;
                        ssPlayer.isStellarRestorationReady = false;
                    }

                });
            }
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

    /**
     * 实现滞空和蓄力判断
     * 客户端通过发包实现。
     */
    public static void onPlayerTick(TickEvent.PlayerTickEvent event){
        Player player = event.player;
        player.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {

            //滞空
            if(ssPlayer.stayInAirTick > 0){
//                player.teleportTo(player.getX(), player.getY()+0.1, player.getZ());
                player.setDeltaMovement(player.getDeltaMovement().scale(0.01));
                ssPlayer.stayInAirTick--;
            }

            //蓄力判断
            //isStellarRestorationReady 可以理解为用来判断是否是第二次按下
            // isStellarRestoration 用来判断是否按下
            player.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).ifPresent(entityPatch -> {
                if(entityPatch instanceof LocalPlayerPatch caster){

                    if(caster.getSkill(ModSkills.STELLAR_RESTORATION) == null){
                        return;
                    }

                    boolean isDodgeKeyPress = EpicFightKeyMappings.DODGE.isDown();

                    //奇数次按就播放动画。需要一直播，防止中断
                    if(isDodgeKeyPress && !ssPlayer.isStellarRestorationReady){
                        caster.playAnimationSynchronized(ModAnimations.STELLAR_RESTORATION_PRE0, 0.0F);
                        player.setYBodyRot(player.getYHeadRot());//不这样的话头动身不动
                        player.setDeltaMovement(Vec3.ZERO);
//                        PacketRelay.sendToServer(PacketHandler.INSTANCE, new StartPreStellarRestorationPacket(true));
                    }

                    //isStellarRestorationReady表示第二次按。如果是第二次按就清空状态。如果是第一次按下那就先标记为true，为上面那个铺垫
                    if(isDodgeKeyPress){
                        ssPlayer.isStellarRestoration = true;
                        return;
                    }

                    //如果isStellarRestoration是true而且没按下，说明是按下后松开了
                    if(ssPlayer.isStellarRestoration){
//                    System.out.println("Released");
                        //第二次按下则不执行发包
                        if(ssPlayer.isStellarRestorationReady){
                            ssPlayer.isStellarRestoration = false;
                            ssPlayer.isStellarRestorationReady = false;
                            return;
                        }

                        PacketRelay.sendToServer(PacketHandler.INSTANCE, new StartPreStellarRestorationPacket(false));
                        //客户端请求服务端执行技能的最好方法
                        caster.getSkill(ModSkills.STELLAR_RESTORATION).sendExecuteRequest(caster, ClientEngine.getInstance().controllEngine);

                        ssPlayer.isStellarRestoration = false;
                        ssPlayer.isStellarRestorationReady = true;
                    }
                }
            });

        });
    }

    public void summonSword(ServerPlayer player, SSPlayer ssPlayer){
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

}
