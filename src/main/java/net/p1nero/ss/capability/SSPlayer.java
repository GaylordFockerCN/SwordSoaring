package net.p1nero.ss.capability;

import net.minecraft.nbt.CompoundTag;

public class SSPlayer {
    private boolean isFlying;
    private boolean protectNextFall;
    private boolean hasEntity;
    private int lastJumpTick;
    private int flyingTick;

    public boolean isCoolDown;

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

    public boolean isHasEntity() {
        return hasEntity;
    }

    public void setHasEntity(boolean hasEntity) {
        this.hasEntity = hasEntity;
    }

    public int getLastJumpTick() {
        return lastJumpTick;
    }

    public void setLastJumpTick(int lastJumpTick) {
        this.lastJumpTick = lastJumpTick;
    }

    public int getFlyingTick() {
        return flyingTick;
    }

    public void setFlyingTick(int flyingTick) {
        this.flyingTick = flyingTick;
    }

    public void saveNBTData(CompoundTag tag){
        tag.putBoolean("isFlying", isFlying);
        tag.putBoolean("protectNextFall", protectNextFall);
        tag.putBoolean("hasEntity", hasEntity);
        tag.putBoolean("isCoolDown",isCoolDown);
        tag.putInt("lastJumpTick", lastJumpTick);
        tag.putInt("flyingTick", flyingTick);
    }

    public void loadNBTData(CompoundTag tag){
        isFlying = tag.getBoolean("isFlying");
        protectNextFall = tag.getBoolean("protectNextFall");
        hasEntity = tag.getBoolean("hasEntity");
        isCoolDown = tag.getBoolean("isCoolDown");
        lastJumpTick= tag.getInt("lastJumpTick");
        flyingTick = tag.getInt("flyingTick");
    }

    public void copyFrom(SSPlayer old){
        isFlying = old.isFlying;
        protectNextFall = old.protectNextFall;
        hasEntity = old.hasEntity;
        isCoolDown = old.isCoolDown;
        lastJumpTick = old.lastJumpTick;
        flyingTick = old.flyingTick;
    }

}
