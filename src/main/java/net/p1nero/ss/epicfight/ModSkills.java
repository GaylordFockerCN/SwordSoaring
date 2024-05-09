package net.p1nero.ss.epicfight;

import net.p1nero.ss.SwordSoaring;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.api.forgeevent.SkillBuildEvent;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.guard.GuardSkill;

public class ModSkills {

    public static Skill SWORD_SOARING;
    public static Skill RAIN_SCREEN;
    public static Skill RAIN_CUTTER;
    public static void registerSkills() {
        SkillManager.register(RainCutter::new, Skill.createBuilder().setResource(Skill.Resource.NONE).setCategory(SkillCategories.IDENTITY), SwordSoaring.MOD_ID, "rain_cutter");
        SkillManager.register(RainScreen::new, Skill.createBuilder().setResource(Skill.Resource.NONE).setCategory(SkillCategories.GUARD), SwordSoaring.MOD_ID, "rain_screen");
        SkillManager.register(SwordSoaringSkill::new, Skill.createMoverBuilder(), SwordSoaring.MOD_ID, "sword_soaring");
    }

    public static void BuildSkills(SkillBuildEvent event){
        RAIN_CUTTER = event.build(SwordSoaring.MOD_ID, "rain_cutter");
        SWORD_SOARING = event.build(SwordSoaring.MOD_ID, "sword_soaring");
        RAIN_SCREEN =  event.build(SwordSoaring.MOD_ID, "rain_screen");
    }

}
