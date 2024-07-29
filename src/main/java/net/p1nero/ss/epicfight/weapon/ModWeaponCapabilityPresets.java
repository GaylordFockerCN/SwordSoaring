package net.p1nero.ss.epicfight.weapon;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.p1nero.ss.SwordSoaring;
import net.p1nero.ss.epicfight.animation.ModAnimations;
import net.p1nero.ss.epicfight.skill.ModSkills;
import reascer.wom.gameasset.WOMAnimations;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.forgeevent.WeaponCapabilityPresetRegistryEvent;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

import java.util.function.Function;

@Mod.EventBusSubscriber(modid = SwordSoaring.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModWeaponCapabilityPresets {
    //Loong's Roar 匣里龙吟
    public static final Function<Item, CapabilityItem.Builder> LOONG_ROAR = (item) ->
            (CapabilityItem.Builder) WeaponCapability.builder().category(ModWeaponCategories.LOONG_ROAR)
            .styleProvider((playerPatch) -> CapabilityItem.Styles.TWO_HAND).collider(ModColliders.LOONG_ROAR)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .hitParticle(EpicFightParticles.HIT_BLADE.get())
            .canBePlacedOffhand(false)
            .newStyleCombo(CapabilityItem.Styles.TWO_HAND,
                    ModAnimations.LOONG_ROAR_AUTO1,
                    ModAnimations.LOONG_ROAR_AUTO2,
                    ModAnimations.LOONG_ROAR_AUTO3,
                    ModAnimations.LOONG_ROAR_AUTO4,
                    ModAnimations.LOONG_ROAR_AUTO5,
                    ModAnimations.LOONG_ROAR_AUTO5,
                    ModAnimations.LOONG_ROAR_AUTO3)
            .innateSkill(CapabilityItem.Styles.TWO_HAND, (itemstack) -> ModSkills.LOONG_ROAR_CHARGED_ATTACK)
            .comboCancel((style) -> false)
            .livingMotionModifier(CapabilityItem.Styles.TWO_HAND,
                    LivingMotions.IDLE,
                    ModAnimations.LOONG_ROAR_IDLE)
            .livingMotionModifier(CapabilityItem.Styles.TWO_HAND,
                    LivingMotions.WALK,
                    Animations.BIPED_WALK_LONGSWORD)
            .livingMotionModifier(CapabilityItem.Styles.TWO_HAND,
                    LivingMotions.CHASE,
                    Animations.BIPED_RUN_LONGSWORD)
            .livingMotionModifier(CapabilityItem.Styles.TWO_HAND,
                    LivingMotions.RUN,
                    Animations.BIPED_RUN_LONGSWORD)
            .livingMotionModifier(CapabilityItem.Styles.TWO_HAND,
                    LivingMotions.SWIM,
                    ModAnimations.LOONG_ROAR_HOLD)
            .livingMotionModifier(CapabilityItem.Styles.TWO_HAND,
                    LivingMotions.BLOCK,
                    Animations.LONGSWORD_GUARD);

    @SubscribeEvent
    public static void register(WeaponCapabilityPresetRegistryEvent event) {
        event.getTypeEntry().put(new ResourceLocation(SwordSoaring.MOD_ID, "loong_roar"), LOONG_ROAR);
    }

}
