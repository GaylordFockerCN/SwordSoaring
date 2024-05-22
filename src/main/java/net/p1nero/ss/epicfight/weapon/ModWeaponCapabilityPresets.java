package net.p1nero.ss.epicfight.weapon;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.p1nero.ss.SwordSoaring;
import reascer.wom.gameasset.WOMAnimations;
import reascer.wom.gameasset.WOMColliders;
import reascer.wom.gameasset.WOMSkills;
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
    //Lion's Roar 匣里龙吟
    public static final Function<Item, CapabilityItem.Builder> STAFF = (item) -> {
        return (CapabilityItem.Builder) WeaponCapability.builder().category(CapabilityItem.WeaponCategories.SPEAR)
                .styleProvider((playerPatch) -> CapabilityItem.Styles.TWO_HAND).collider(WOMColliders.STAFF)
                .hitSound(EpicFightSounds.BLUNT_HIT.get())
                .hitParticle(EpicFightParticles.HIT_BLUNT.get())
                .canBePlacedOffhand(false)
                .newStyleCombo(CapabilityItem.Styles.TWO_HAND,
                        WOMAnimations.STAFF_AUTO_1,
                        WOMAnimations.STAFF_AUTO_2,
                        WOMAnimations.STAFF_AUTO_3,
                        WOMAnimations.STAFF_DASH,
                        WOMAnimations.STAFF_KINKONG)
                .newStyleCombo(CapabilityItem.Styles.MOUNT, Animations.SPEAR_MOUNT_ATTACK)
                .innateSkill(CapabilityItem.Styles.TWO_HAND, (itemstack) -> WOMSkills.CHARYBDIS)
                .comboCancel((style) -> false)
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND,
                        LivingMotions.IDLE,
                        WOMAnimations.STAFF_IDLE)
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND,
                        LivingMotions.WALK,
                        Animations.BIPED_HOLD_SPEAR)
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND,
                        LivingMotions.CHASE,
                        WOMAnimations.STAFF_RUN)
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND,
                        LivingMotions.RUN,
                        WOMAnimations.STAFF_RUN)
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND,
                        LivingMotions.SWIM,
                        Animations.BIPED_HOLD_SPEAR)
                .livingMotionModifier(CapabilityItem.Styles.TWO_HAND,
                        LivingMotions.BLOCK,
                        Animations.SPEAR_GUARD);
    };

    @SubscribeEvent
    public static void register(WeaponCapabilityPresetRegistryEvent event) {
        event.getTypeEntry().put(new ResourceLocation(SwordSoaring.MOD_ID, "test"), STAFF);
    }

}
