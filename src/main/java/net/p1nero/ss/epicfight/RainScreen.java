package net.p1nero.ss.epicfight;

import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.guard.GuardSkill;

/**
 * 画雨笼山
 */
public class RainScreen extends GuardSkill {
    public RainScreen(Builder builder) {
        super(builder);
    }

    @Override
    public Skill getPriorSkill() {
        return ModSkills.SWORD_SOARING;
    }
}
