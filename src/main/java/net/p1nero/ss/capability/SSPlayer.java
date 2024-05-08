package net.p1nero.ss.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.p1nero.ss.Config;

public class SSPlayer {
    private boolean isFlying;
    private boolean protectNextFall;
    private boolean hasSwordEntity;
    private int anticipationTick;
    private ItemStack sword;

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

    public ItemStack getSword() {
        return sword;
    }

    public void setSword(ItemStack sword) {
        this.sword = sword;
    }

    public void putAwaySword(ServerPlayer player){
        if(Config.HIDE_SWORD_WHEN_FLY.get() && getSword().isEmpty()){
            setSword(player.getMainHandItem());
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
    }

    public void returnSword(ServerPlayer player){
        if(Config.HIDE_SWORD_WHEN_FLY.get()){
            if(!player.addItem(getSword())){
                player.drop(getSword(),true);
            }
            setSword(ItemStack.EMPTY);
        }
    }

    public void saveNBTData(CompoundTag tag){
        tag.putBoolean("isFlying", isFlying);
        tag.putBoolean("protectNextFall", protectNextFall);
        tag.putBoolean("hasEntity", hasSwordEntity);
        tag.putInt("anticipationTick", anticipationTick);
        tag.put("sword", sword.serializeNBT());
    }

    public void loadNBTData(CompoundTag tag){
        isFlying = tag.getBoolean("isFlying");
        protectNextFall = tag.getBoolean("protectNextFall");
        hasSwordEntity = tag.getBoolean("hasEntity");
        anticipationTick = tag.getInt("anticipationTick");
        sword = ItemStack.of(tag.getCompound("sword"));
    }

    public void copyFrom(SSPlayer old){
        isFlying = old.isFlying;
        protectNextFall = old.protectNextFall;
        hasSwordEntity = old.hasSwordEntity;
        anticipationTick = old.anticipationTick;
    }

}
