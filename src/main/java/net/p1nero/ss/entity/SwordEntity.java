package net.p1nero.ss.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

import static net.p1nero.ss.util.ItemStackUtil.*;

public class SwordEntity extends Entity {
    private ItemStack itemStack;
    private Player rider;
    public SwordEntity(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
        itemStack = ItemStack.EMPTY;
    }
    public SwordEntity(ItemStack itemStack, Player rider) {
        super(ModEntities.SWORD.get(), rider.level());
        this.itemStack = itemStack;
        this.rider = rider;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public void setRider(Player rider) {
        this.rider = rider;
    }

    public void stopRiding() {
        this.rider = null;
    }

    @Override
    public void tick() {
        super.tick();
        if(rider == null){
            System.out.println(level());
            discard();
            return;
        }
        setPos(new Vec3(rider.getX(),rider.getY(),rider.getZ()));
        setYRot(rider.getYRot());
        //TODO 优化一下，统一到服务端
        if(rider.getDeltaMovement().length() < 0.079){
            discard();
        }
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag p_20052_) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag p_20139_) {

    }

}
