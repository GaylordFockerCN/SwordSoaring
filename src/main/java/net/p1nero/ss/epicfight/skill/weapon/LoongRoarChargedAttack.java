package net.p1nero.ss.epicfight.skill.weapon;

import net.minecraft.network.FriendlyByteBuf;
import net.p1nero.ss.epicfight.animation.ModAnimations;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.weaponinnate.WeaponInnateSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

/**
 * 重击
 * TODO 改为长按左键实现
 */
public class LoongRoarChargedAttack extends WeaponInnateSkill {
    public LoongRoarChargedAttack(Builder<? extends Skill> builder) {
        super(builder);
    }

    @Override
    public void executeOnServer(ServerPlayerPatch executer, FriendlyByteBuf args) {
        executer.playAnimationSynchronized(ModAnimations.LOONG_ROAR_HEAVY, 0.0F);
        super.executeOnServer(executer, args);
    }
}
