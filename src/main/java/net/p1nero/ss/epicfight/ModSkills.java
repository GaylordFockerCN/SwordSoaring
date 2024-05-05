package net.p1nero.ss.epicfight;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.p1nero.ss.SwordSoaring;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.api.forgeevent.SkillBuildEvent;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.passive.PassiveSkill;

//@Mod.EventBusSubscriber(modid = SwordSoaring.MOD_ID, bus= Mod.EventBusSubscriber.Bus.FORGE)
public class ModSkills {

    public static Skill SWORD_SOARING;
    public static void registerSkills() {
        SkillManager.register(SwordSoaringSkill::new, Skill.createMoverBuilder().setResource(Skill.Resource.COOLDOWN), SwordSoaring.MOD_ID, "sword_soaring");
    }
//    @SubscribeEvent
    public static void BuildSkills(SkillBuildEvent event){
        SWORD_SOARING = event.build(SwordSoaring.MOD_ID, "sword_soaring");
    }

}
