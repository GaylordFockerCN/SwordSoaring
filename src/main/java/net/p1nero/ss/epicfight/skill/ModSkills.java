package net.p1nero.ss.epicfight.skill;

import net.p1nero.ss.SwordSoaring;
import net.p1nero.ss.epicfight.skill.weapon.LoongRoarChargedAttack;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.api.forgeevent.SkillBuildEvent;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.weaponinnate.WeaponInnateSkill;

public class ModSkills {

    public static Skill SWORD_SOARING;
    public static Skill RAIN_SCREEN;
    public static Skill RAIN_CUTTER;
    public static Skill YAKSHA_MASK;
    public static Skill STELLAR_RESTORATION;
    public static Skill SWORD_CONVERGENCE;
    public static Skill LOONG_ROAR_CHARGED_ATTACK;
    public static void registerSkills() {
        SkillManager.register(SwordSoaringSkill::new, Skill.createMoverBuilder().setResource(Skill.Resource.COOLDOWN), SwordSoaring.MOD_ID, "sword_soaring");
        SkillManager.register(RainCutter::new, Skill.createBuilder().setResource(Skill.Resource.NONE).setCategory(SkillCategories.IDENTITY), SwordSoaring.MOD_ID, "rain_cutter");
        SkillManager.register(RainScreen::new, Skill.createBuilder().setResource(Skill.Resource.NONE).setCategory(SkillCategories.GUARD), SwordSoaring.MOD_ID, "rain_screen");
        SkillManager.register(YakshaMask::new, YakshaMask.createYakshaMaskBuilder(), SwordSoaring.MOD_ID, "yaksha_mask");
        SkillManager.register(StellarRestoration::new, Skill.createBuilder().setResource(Skill.Resource.NONE).setCategory(SkillCategories.DODGE), SwordSoaring.MOD_ID, "stellar_restoration");
        SkillManager.register(SwordConvergence::new, Skill.createBuilder().setResource(Skill.Resource.NONE).setCategory(SkillCategories.IDENTITY), SwordSoaring.MOD_ID, "sword_convergence");

        SkillManager.register(LoongRoarChargedAttack::new, WeaponInnateSkill.createWeaponInnateBuilder(), SwordSoaring.MOD_ID, "loong_roar_charged_attack");

    }

    public static void BuildSkills(SkillBuildEvent event){
        SWORD_SOARING = event.build(SwordSoaring.MOD_ID, "sword_soaring");
        RAIN_CUTTER = event.build(SwordSoaring.MOD_ID, "rain_cutter");
        YAKSHA_MASK = event.build(SwordSoaring.MOD_ID, "yaksha_mask");
        RAIN_SCREEN =  event.build(SwordSoaring.MOD_ID, "rain_screen");
        STELLAR_RESTORATION = event.build(SwordSoaring.MOD_ID, "stellar_restoration");
        SWORD_CONVERGENCE = event.build(SwordSoaring.MOD_ID, "sword_convergence");

        LOONG_ROAR_CHARGED_ATTACK = event.build(SwordSoaring.MOD_ID, "loong_roar_charged_attack");
    }

}
