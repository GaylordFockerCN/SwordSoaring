package net.p1nero.ss.util;

import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataKey;

public class SkillDataUtil {
    public static void registerSkillData(SkillContainer container, SkillDataKey<?>...skillDataKey){
        for(SkillDataKey<?> key : skillDataKey){
            if(!container.getDataManager().hasData(key)){
                container.getDataManager().registerData(key);
            }
        }
    }

    public static void removeSkillData(SkillContainer container, SkillDataKey<?>...skillDataKey){
        for(SkillDataKey<?> key : skillDataKey){
            if(container.getDataManager().hasData(key)){
                container.getDataManager().removeData(key);
            }
        }
    }
}
