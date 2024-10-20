package net.p1nero.ss.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public class Boss extends Monster {
    protected Boss(EntityType<? extends Monster> type, Level level) {
        super(type, level);

    }



}
