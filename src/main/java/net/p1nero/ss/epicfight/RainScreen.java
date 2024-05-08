package net.p1nero.ss.epicfight;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import yesman.epicfight.api.animation.AnimationProvider;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSkills;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.skill.*;
import yesman.epicfight.skill.guard.GuardSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.entity.eventlistener.HurtEvent;

/**
 * 画雨笼山
 */
public class RainScreen extends GuardSkill {
    public RainScreen(Builder builder) {
        super(builder);
    }

    /**
     * 直接抄了改，用super会有bug
     */
    @Override
    public void guard(SkillContainer container, CapabilityItem itemCapability, HurtEvent.Pre event, float knockback, float impact, boolean advanced) {
        DamageSource damageSource = event.getDamageSource();
        if (this.isBlockableSource(damageSource, advanced)) {
            event.getPlayerPatch().playSound(EpicFightSounds.CLASH.get(), -0.05F, 0.1F);
            ServerPlayer serveerPlayer = event.getPlayerPatch().getOriginal();
            EpicFightParticles.HIT_BLUNT.get().spawnParticleWithArgument(serveerPlayer.serverLevel(), HitParticleType.FRONT_OF_EYES, HitParticleType.ZERO, serveerPlayer, damageSource.getDirectEntity());
            Entity var10 = damageSource.getDirectEntity();
            if (var10 instanceof LivingEntity livingEntity) {
                knockback += (float) EnchantmentHelper.getKnockbackBonus(livingEntity) * 0.1F;
            }

            event.getPlayerPatch().knockBackEntity(damageSource.getDirectEntity().position(), knockback);
            event.getPlayerPatch().consumeStaminaAlways(1);
            BlockType blockType = event.getPlayerPatch().hasStamina(0.0F) ? GuardSkill.BlockType.GUARD : GuardSkill.BlockType.GUARD_BREAK;
            StaticAnimation animation = this.getGuardMotion(event.getPlayerPatch(), itemCapability, blockType);
            if (animation != null) {
                event.getPlayerPatch().playAnimationSynchronized(animation, 0.0F);
            }

            if (blockType == GuardSkill.BlockType.GUARD_BREAK) {
                event.getPlayerPatch().playSound(EpicFightSounds.NEUTRALIZE_MOBS.get(), 3.0F, 0.0F, 0.1F);
            }

            this.dealEvent(event.getPlayerPatch(), event, advanced);
        }
        container.getExecuter().playAnimationSynchronized(Animations.BIPED_PHANTOM_ASCENT_BACKWARD, 0);
    }

    @Override
    public Skill getPriorSkill() {
        return EpicFightSkills.GUARD;
    }

    @Override
    protected boolean isAdvancedGuard() {
        return true;
    }
}
