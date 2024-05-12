package net.p1nero.ss.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.p1nero.ss.Config;

import java.util.HashSet;
import java.util.Set;

/**
 * 记录飞行和技能使用的状态，被坑了，这玩意儿也分服务端和客户端...
 */
public class SSPlayer {
    private boolean isFlying;
    private boolean protectNextFall;
    private boolean hasSwordEntity;
    private int swordScreenEntityCount;
    private int rainCutterTimer;
    private boolean isScreenCutterCoolDown;
    private int yakshaMaskTimer;
    public boolean canYakshaMask;
    public boolean isYakshaFall;
    private Set<Integer> swordID;
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

    public int getSwordScreenEntityCount() {
        return swordScreenEntityCount;
    }

    public void setSwordScreenEntityCount(int swordScreenEntityCount) {
        if(swordScreenEntityCount < 0){
            return;
        }
        this.swordScreenEntityCount = swordScreenEntityCount;
    }

    public int getRainCutterTimer() {
        return rainCutterTimer;
    }

    public void setRainCutterTimer(int rainCutterTimer) {
        this.rainCutterTimer = rainCutterTimer;
    }

    public boolean isScreenCutterCoolDown() {
        return isScreenCutterCoolDown;
    }

    public int getYakshaMaskTimer() {
        return yakshaMaskTimer;
    }

    public void setYakshaMaskTimer(int yakshaMaskTimer) {
        this.yakshaMaskTimer = yakshaMaskTimer;
    }

    public void setScreenCutterCoolDown(boolean screenCutterCoolDown) {
        this.isScreenCutterCoolDown = screenCutterCoolDown;
    }

    public void setSwordID(Set<Integer> swordID) {
        this.swordID = swordID;
    }

    public Set<Integer> getSwordID() {
        if(swordID == null){
            swordID = new HashSet<>();
        }
        return swordID;
    }

    public int getAnticipationTick() {
        return anticipationTick;
    }

    public void setAnticipationTick(int anticipationTick) {
        this.anticipationTick = anticipationTick;
    }

    public ItemStack getSword() {
        if(sword == null){
            return ItemStack.EMPTY;
        }
        return sword;
    }

    public void setSword(ItemStack sword) {
        this.sword = sword;
    }

    public void putAwaySword(ServerPlayer player){
        if(getSword().isEmpty()){
            setSword(player.getMainHandItem());
            if(Config.HIDE_SWORD_WHEN_FLY.get()){
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            }
        }

    }

    public void returnSword(ServerPlayer player){
        if(Config.HIDE_SWORD_WHEN_FLY.get()){
            if(!player.addItem(getSword())){
                player.drop(getSword(),true);
            }
        }
        setSword(ItemStack.EMPTY);
    }

    public void saveNBTData(CompoundTag tag){
        tag.putBoolean("isFlying", isFlying);
        tag.putBoolean("protectNextFall", protectNextFall);
        tag.putBoolean("hasEntity", hasSwordEntity);
        tag.putInt("hasSwordScreenEntity", swordScreenEntityCount);
        tag.putInt("rainCutterTimer", rainCutterTimer);
        tag.putBoolean("rainCutterCoolDown", isScreenCutterCoolDown);
        tag.putInt("yakshaMaskTimer", yakshaMaskTimer);
        tag.putInt("anticipationTick", anticipationTick);
        if(sword != null){
            tag.put("sword", sword.serializeNBT());
        }else {
            tag.put("sword", new CompoundTag());
        }
    }

    public void loadNBTData(CompoundTag tag){
        isFlying = tag.getBoolean("isFlying");
        protectNextFall = tag.getBoolean("protectNextFall");
        hasSwordEntity = tag.getBoolean("hasEntity");
        swordScreenEntityCount = tag.getInt("hasSwordScreenEntity");
        rainCutterTimer = tag.getInt("rainCutterTimer");
        isScreenCutterCoolDown = tag.getBoolean("rainCutterCoolDown");
        yakshaMaskTimer = tag.getInt("yakshaMaskTimer");
        anticipationTick = tag.getInt("anticipationTick");
        sword = ItemStack.of(tag.getCompound("sword"));
    }

    public void copyFrom(SSPlayer old){
        isFlying = old.isFlying;
        protectNextFall = old.protectNextFall;
        hasSwordEntity = old.hasSwordEntity;
        swordScreenEntityCount = old.swordScreenEntityCount;
        rainCutterTimer = old.rainCutterTimer;
        isScreenCutterCoolDown = old.isScreenCutterCoolDown;
        yakshaMaskTimer = old.yakshaMaskTimer;
        anticipationTick = old.anticipationTick;
        sword = old.sword;
    }

}
