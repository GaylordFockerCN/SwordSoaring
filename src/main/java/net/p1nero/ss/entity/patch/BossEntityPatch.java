package net.p1nero.ss.entity.patch;

import net.p1nero.ss.entity.Boss;
import net.p1nero.ss.epicfight.animation.ModAnimations;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.Faction;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;

public class BossEntityPatch extends HumanoidMobPatch<Boss> {
    public BossEntityPatch(Faction faction) {
        super(faction);
    }

    @Override
    public void initAnimator(Animator animator) {
        animator.addLivingAnimation(LivingMotions.IDLE, ModAnimations.FLY_ON_SWORD_ADVANCED);
        animator.addLivingAnimation(LivingMotions.WALK, ModAnimations.FLY_ON_SWORD_ADVANCED);
        animator.addLivingAnimation(LivingMotions.CHASE, ModAnimations.FLY_ON_SWORD_BASIC);
        animator.addLivingAnimation(LivingMotions.FALL, Animations.BIPED_FALL);
        animator.addLivingAnimation(LivingMotions.MOUNT, Animations.BIPED_MOUNT);
        animator.addLivingAnimation(LivingMotions.DEATH, Animations.BIPED_DEATH);
    }

    @Override
    public void updateMotion(boolean b) {
        super.commonAggressiveMobUpdateMotion(b);
    }
}
