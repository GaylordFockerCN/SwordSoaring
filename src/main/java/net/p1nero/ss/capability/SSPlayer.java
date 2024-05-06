package net.p1nero.ss.capability;

import net.minecraft.nbt.CompoundTag;

public class SSPlayer {
    private boolean isFlying;
    private boolean protectNextFall;
    private boolean hasSwordEntity;
    private int anticipationTick;

    public boolean isFlying() {
        return isFlying;
    }

    public void setFlying(boolean flying) {
        isFlying = flying;
    }

    public boolean isProtectNextFall() {
        return protectNextFall;
    }

    public void setProtectNextFall(boolean protectNextFall) {
        this.protectNextFall = protectNextFall;
    }

    public boolean hasSwordEntity() {
        return hasSwordEntity;
    }

    public void setHasSwordEntity(boolean hasSwordEntity) {
        this.hasSwordEntity = hasSwordEntity;
    }

    public int getAnticipationTick() {
        return anticipationTick;
    }

    public void setAnticipationTick(int anticipationTick) {
        this.anticipationTick = anticipationTick;
    }

    public void saveNBTData(CompoundTag tag){
        tag.putBoolean("isFlying", isFlying);
        tag.putBoolean("protectNextFall", protectNextFall);
        tag.putBoolean("hasEntity", hasSwordEntity);
        tag.putInt("anticipationTick", anticipationTick);
    }

    public void loadNBTData(CompoundTag tag){
        isFlying = tag.getBoolean("isFlying");
        protectNextFall = tag.getBoolean("protectNextFall");
        hasSwordEntity = tag.getBoolean("hasEntity");
        anticipationTick = tag.getInt("anticipationTick");
    }

    public void copyFrom(SSPlayer old){
        isFlying = old.isFlying;
        protectNextFall = old.protectNextFall;
        hasSwordEntity = old.hasSwordEntity;
        anticipationTick = old.anticipationTick;
    }

}
