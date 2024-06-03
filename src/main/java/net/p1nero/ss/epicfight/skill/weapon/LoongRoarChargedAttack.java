package net.p1nero.ss.epicfight.skill.weapon;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.p1nero.ss.epicfight.animation.ModAnimations;
import net.p1nero.ss.epicfight.weapon.ModWeaponCategories;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.weaponinnate.WeaponInnateSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

/**
 * 重击
 */
public class LoongRoarChargedAttack extends WeaponInnateSkill {
    public LoongRoarChargedAttack(Builder<? extends Skill> builder) {
        super(builder);
    }

    @Override
    public void executeOnServer(ServerPlayerPatch executer, FriendlyByteBuf args) {
        //防止拿着物品也会触发。我也不知道为什么有这个bug。
        if(executer.getHoldingItemCapability(InteractionHand.MAIN_HAND).getWeaponCategory() != ModWeaponCategories.LOONG_ROAR){
            return;
        }
        executer.playAnimationSynchronized(ModAnimations.LOONG_ROAR_HEAVY_ALL, 0.0F);
        super.executeOnServer(executer, args);
    }

    @Override
    public boolean canExecute(PlayerPatch<?> executer) {
        return true;
    }
}
