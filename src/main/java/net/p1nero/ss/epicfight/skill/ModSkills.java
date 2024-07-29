package net.p1nero.ss.epicfight.skill;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.p1nero.ss.SwordSoaring;
import net.p1nero.ss.epicfight.skill.weapon.LoongRoarChargedAttack;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.api.forgeevent.SkillBuildEvent;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.weaponinnate.WeaponInnateSkill;

@Mod.EventBusSubscriber(modid = SwordSoaring.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModSkills {

    public static Skill SWORD_SOARING;
    public static Skill RAIN_SCREEN;
    public static Skill RAIN_CUTTER;
    public static Skill YAKSHA_MASK;
    public static Skill STELLAR_RESTORATION;
    public static Skill SWORD_CONVERGENCE;
    public static Skill LOONG_ROAR_CHARGED_ATTACK;


    @SubscribeEvent
    public static void BuildSkills(SkillBuildEvent event)
    {
        SkillBuildEvent.ModRegistryWorker registryWorker = event.createRegistryWorker(SwordSoaring.MOD_ID);

        SWORD_SOARING = registryWorker.build("sword_soaring", SwordSoaringSkill::new, Skill.createMoverBuilder().setResource(Skill.Resource.NONE));
        RAIN_CUTTER = registryWorker.build("rain_cutter", RainCutter::new, Skill.createBuilder().setResource(Skill.Resource.NONE).setCategory(SkillCategories.IDENTITY));
        YAKSHA_MASK = registryWorker.build("yaksha_mask", YakshaMask::new, YakshaMask.createYakshaMaskBuilder());
        RAIN_SCREEN = registryWorker.build("rain_screen", RainScreen::new, Skill.createBuilder().setResource(Skill.Resource.NONE).setCategory(SkillCategories.GUARD));
        STELLAR_RESTORATION = registryWorker.build("stellar_restoration", StellarRestoration::new, Skill.createBuilder().setResource(Skill.Resource.NONE).setCategory(SkillCategories.DODGE));
        SWORD_CONVERGENCE = registryWorker.build("sword_convergence", SwordConvergence::new, Skill.createBuilder().setResource(Skill.Resource.NONE).setCategory(SkillCategories.IDENTITY));
        LOONG_ROAR_CHARGED_ATTACK = registryWorker.build("loong_roar_charged_attack", LoongRoarChargedAttack::new, WeaponInnateSkill.createWeaponInnateBuilder().setResource(Skill.Resource.NONE));
    }

}
