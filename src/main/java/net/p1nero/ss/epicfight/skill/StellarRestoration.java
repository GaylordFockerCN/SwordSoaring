package net.p1nero.ss.epicfight.skill;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.Vec3;
import net.p1nero.ss.SwordSoaring;
import net.p1nero.ss.capability.SSCapabilityProvider;
import net.p1nero.ss.capability.SSPlayer;
import net.p1nero.ss.entity.RainCutterSwordEntity;
import net.p1nero.ss.entity.StellarSwordEntity;
import net.p1nero.ss.epicfight.animation.ModAnimations;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
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

                    if(!ssPlayer.isStellarRestoration){
                        if(player instanceof ServerPlayer serverPlayer && event.getPlayerPatch().hasStamina(4)){
                            if(!player.isCreative()){
                                event.getPlayerPatch().consumeStamina(4);
                            }
                            summonSword(serverPlayer, ssPlayer);
                            event.getPlayerPatch().playAnimationSynchronized(ModAnimations.STELLAR_RESTORATION_PRE,0);
                            ssPlayer.isStellarRestoration = true;
                            ssPlayer.setProtectNextFall(true);
                        }
                    }else {
                        Entity sword = player.level().getEntity(ssPlayer.stellarSwordID);
                        if(sword != null){
                            player.teleportTo(sword.getX(), sword.getY(), sword.getZ());
                            sword.discard();
                            event.getPlayerPatch().playAnimationSynchronized(Animations.SWEEPING_EDGE,0);
                        }
                        //不管能不能瞬移都要重置
                        ssPlayer.isStellarRestoration = false;
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
