package net.p1nero.ss.epicfight.animation;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.p1nero.ss.Config;
import net.p1nero.ss.SwordSoaring;
import net.p1nero.ss.capability.SSCapabilityProvider;
import reascer.wom.animation.attacks.SpecialAttackAnimation;
import reascer.wom.gameasset.WOMColliders;
import reascer.wom.world.damagesources.WOMExtraDamageInstance;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.*;
import yesman.epicfight.api.collider.OBBCollider;
import yesman.epicfight.api.forgeevent.AnimationRegistryEvent;
import yesman.epicfight.api.utils.TimePairList;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.model.armature.HumanoidArmature;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageType;
import yesman.epicfight.world.damagesource.StunType;

import java.util.Set;

import static net.p1nero.ss.epicfight.skill.RainScreen.summonSword;

@Mod.EventBusSubscriber(modid = SwordSoaring.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModAnimations {
    public static StaticAnimation PLUNGING_ATTACK;
    public static StaticAnimation JUMP_TO_SWORD;
    public static StaticAnimation FLY_ON_SWORD;
    public static StaticAnimation FLY_ON_SWORD_BASIC;
    public static StaticAnimation FLY_ON_SWORD_ADVANCED;
    public static StaticAnimation RAIN_SCREEN;
    public static StaticAnimation STELLAR_RESTORATION_PRE;
    public static StaticAnimation STELLAR_RESTORATION_PRE0;
    public static StaticAnimation AGONY_PLUNGE_FORWARD;

    @SubscribeEvent
    public static void registerAnimations(AnimationRegistryEvent event) {
        event.getRegistryMap().put(SwordSoaring.MOD_ID, ModAnimations::build);
    }

    private static void build() {
        HumanoidArmature biped = Armatures.BIPED;

        RAIN_SCREEN = (new ActionAnimation(0.05F, 0.7F, "biped/rain_screen", biped))
                .addStateRemoveOld(EntityState.MOVEMENT_LOCKED, false).newTimePair(0.0F, 2.0F)
                .addStateRemoveOld(EntityState.INACTION, true)
                .addEvents(AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.create((entitypatch, animation, params) -> {
                    if(entitypatch instanceof LocalPlayerPatch playerPatch){
                        playerPatch.getOriginal().getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
                            ssPlayer.rainScreenCooldownTimer = Config.RAIN_SCREEN_COOLDOWN.get().intValue();
                        });
                    }
            Vec3 pos = entitypatch.getOriginal().position();
            entitypatch.playSound(EpicFightSounds.ROLL.get(), 0.0F, 0.0F);
            entitypatch.getOriginal().level().addAlwaysVisibleParticle(EpicFightParticles.AIR_BURST.get(), pos.x, pos.y + (double) entitypatch.getOriginal().getBbHeight() * 0.5, pos.z, 0.0, -1.0, 2.0);
        }, AnimationEvent.Side.CLIENT))
                .addEvents(AnimationProperty.StaticAnimationProperty.ON_END_EVENTS, AnimationEvent.create((entitypatch, animation, params) -> {
            if(entitypatch instanceof ServerPlayerPatch caster){
                summonSword(caster.getOriginal());
                caster.getOriginal().getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
                    ssPlayer.rainScreenCooldownTimer = Config.RAIN_SCREEN_COOLDOWN.get().intValue();;
                });
            }
        }, AnimationEvent.Side.SERVER));

        STELLAR_RESTORATION_PRE0 = new StaticAnimation(true, "biped/stellar_restoration_pre0", biped)
                .addStateRemoveOld(EntityState.MOVEMENT_LOCKED, true)
                .addStateRemoveOld(EntityState.INACTION, true);

        STELLAR_RESTORATION_PRE = (new ActionAnimation(0.05F, 2.0F, "biped/stellar_restoration_pre", biped)).addStateRemoveOld(EntityState.MOVEMENT_LOCKED, true).newTimePair(0.0F, 2.0F).addStateRemoveOld(EntityState.INACTION, true).addEvents(AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS, new AnimationEvent[]{AnimationEvent.create((entitypatch, animation, params) -> {
            entitypatch.playSound(EpicFightSounds.ROLL.get(), 0.0F, 0.0F);
            }, AnimationEvent.Side.CLIENT)}).addEvents(AnimationProperty.StaticAnimationProperty.ON_END_EVENTS, AnimationEvent.create((entitypatch, animation, params) -> {
            if (entitypatch instanceof PlayerPatch<?> playerpatch) {
                playerpatch.changeModelYRot(0.0F);
            }
        }, AnimationEvent.Side.CLIENT));

        if(ModList.get().isLoaded("wom")){
            AGONY_PLUNGE_FORWARD = (new SpecialAttackAnimation(0.05F, "biped/agony_plunge_forward", biped, new AttackAnimation.Phase(0.0F, 0.1F, 0.2F, 0.25F, 0.25F, biped.rootJoint,  new OBBCollider(5.0, 2.0, 5.0, 0.0, 0.0, 0.0)), new AttackAnimation.Phase(0.25F, 1.1F, 1.45F, 1.7F, Float.MAX_VALUE, biped.rootJoint, WOMColliders.AGONY_PLUNGE))).addProperty(AnimationProperty.AttackPhaseProperty.HIT_SOUND, EpicFightSounds.WHOOSH_BIG.get(), 0).addProperty(AnimationProperty.AttackPhaseProperty.PARTICLE, EpicFightParticles.HIT_BLUNT, 0).addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(10.0F), 0).addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.setter(9.0F), 0).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(1.0F), 0).addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.HOLD, 0).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.2F), 1).addProperty(AnimationProperty.AttackPhaseProperty.EXTRA_DAMAGE, Set.of(WOMExtraDamageInstance.WOM_SWEEPING_EDGE_ENCHANTMENT.create(1.5F), WOMExtraDamageInstance.TARGET_LOST_HEALTH.create(0.2F)), 1).addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.0F), 1).addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(10.0F), 1).addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageType.WEAPON_INNATE), 1).addProperty(AnimationProperty.AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLADE_RUSH_FINISHER.get(), 1).addProperty(AnimationProperty.AttackPhaseProperty.PARTICLE, EpicFightParticles.BLADE_RUSH_SKILL, 1).addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN, 1).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.5F).addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true).addProperty(AnimationProperty.ActionAnimationProperty.STOP_MOVEMENT, false).addProperty(AnimationProperty.ActionAnimationProperty.CANCELABLE_MOVE, false).addProperty(AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(new float[]{0.0F, 1.3F}));
        }

        JUMP_TO_SWORD = (new ActionAnimation(0.15F, 2.0F, "biped/jump_to_sword", biped))
                .addStateRemoveOld(EntityState.MOVEMENT_LOCKED, true).newTimePair(0.0F, 2.0F)
                .addStateRemoveOld(EntityState.INACTION, true)
                .addEvents(AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.create(
                        (entitypatch, animation, params) -> entitypatch.playSound(EpicFightSounds.ROCKET_JUMP.get(), 0.0F, 0.0F), AnimationEvent.Side.CLIENT))
                .addEvents(AnimationProperty.StaticAnimationProperty.ON_END_EVENTS, AnimationEvent.create((entitypatch, animation, params) -> {
        }, AnimationEvent.Side.CLIENT));

        FLY_ON_SWORD_BASIC = new StaticAnimation(false, "biped/fly_on_sword_beginner", biped).addStateRemoveOld(EntityState.CAN_BASIC_ATTACK, true)
                .addStateRemoveOld(EntityState.CAN_SKILL_EXECUTION, true)
                .addStateRemoveOld(EntityState.INACTION, true)
                .addEvents(AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.create((entitypatch, animation, params) -> {
                    if(entitypatch instanceof ServerPlayerPatch serverPlayerPatch){
                        serverPlayerPatch.getOriginal().getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
                            ssPlayer.isPlayingAnim = true;
                        });
                    }
                }, AnimationEvent.Side.SERVER))
                .addEvents(AnimationProperty.StaticAnimationProperty.ON_END_EVENTS, AnimationEvent.create((entitypatch, animation, params) -> {
                    if(entitypatch instanceof ServerPlayerPatch serverPlayerPatch){
                        serverPlayerPatch.getOriginal().getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
                            ssPlayer.isPlayingAnim = false;
                        });
                    }
                }, AnimationEvent.Side.SERVER));

        FLY_ON_SWORD_ADVANCED = new StaticAnimation(false, "biped/fly_on_sword_master", biped).addStateRemoveOld(EntityState.CAN_BASIC_ATTACK, true)
                .addStateRemoveOld(EntityState.CAN_SKILL_EXECUTION, true)
                .addStateRemoveOld(EntityState.INACTION, true)
                .addEvents(AnimationProperty.StaticAnimationProperty.ON_BEGIN_EVENTS, AnimationEvent.create((entitypatch, animation, params) -> {
                    if(entitypatch instanceof ServerPlayerPatch serverPlayerPatch){
                        setPlayingAnim(serverPlayerPatch.getOriginal(), true);
                    }
                }, AnimationEvent.Side.SERVER))
                .addEvents(AnimationProperty.StaticAnimationProperty.ON_END_EVENTS, AnimationEvent.create((entitypatch, animation, params) -> {
                    if(entitypatch instanceof ServerPlayerPatch serverPlayerPatch){
                        setPlayingAnim(serverPlayerPatch.getOriginal(), false);
                    }
        }, AnimationEvent.Side.SERVER));

    }

    public static void setPlayingAnim(Player player, boolean b){
        player.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
            ssPlayer.isPlayingAnim = b;
        });
    }

}
