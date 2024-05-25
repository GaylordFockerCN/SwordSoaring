package net.p1nero.ss.epicfight.weapon;

import yesman.epicfight.world.capabilities.item.WeaponCategory;

public enum ModWeaponCategories implements WeaponCategory {
    LOONG_ROAR;
    private ModWeaponCategories(){
        this.id = WeaponCategory.ENUM_MANAGER.assign(this);
    }
    final int id;
    @Override
    public int universalOrdinal() {
        return this.id;
    }
}
