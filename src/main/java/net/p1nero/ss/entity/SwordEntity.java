package net.p1nero.ss.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;
import net.p1nero.ss.SwordSoaring;
import net.p1nero.ss.capability.SSCapabilityProvider;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.world.item.LongswordItem;
import yesman.epicfight.world.item.TachiItem;
import yesman.epicfight.world.item.UchigatanaItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SwordEntity extends Entity implements AbstractSwordEntity{
    protected Player rider;

    private static final EntityDataAccessor<Optional<UUID>> RIDER_UUID = SynchedEntityData.defineId(SwordEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<ItemStack> ITEM_STACK = SynchedEntityData.defineId(SwordEntity.class, EntityDataSerializers.ITEM_STACK);

    public SwordEntity(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
        this.getEntityData().define(ITEM_STACK, ItemStack.EMPTY);
        this.getEntityData().define(RIDER_UUID, Optional.empty());
    }
    public SwordEntity(ItemStack itemStack, Player rider) {
        super(ModEntities.SWORD.get(), rider.level());
        this.rider = rider;
        this.getEntityData().define(ITEM_STACK, itemStack);
        this.getEntityData().define(RIDER_UUID, Optional.of(rider.getUUID()));
    }

    @Override
    public ItemStack getItemStack() {
        return this.getEntityData().get(ITEM_STACK);
    }

    public void setItemStack(ItemStack itemStack) {
        this.getEntityData().set(ITEM_STACK, itemStack);
    }

    public void setRider(Player rider) {
        this.rider = rider;
        this.getEntityData().set(RIDER_UUID, Optional.of(rider.getUUID()));
    }

    public void stopRiding() {
        this.rider = null;
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float p_19947_) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if(rider == null){
            //呃呃呃简单粗暴，后面才想到的。要个屁的UUID同步，妈的
            if(level().isClientSide){
                rider = Minecraft.getInstance().player;
            }
            if(this.getEntityData().get(RIDER_UUID).isPresent()){
                rider = level().getPlayerByUUID(this.getEntityData().get(RIDER_UUID).get());
            }else {
                SwordSoaring.LOGGER.info("sword entity "+ getId() + " doesn't have rider "+level());
                discard();
                return;
            }
        }

        if(!level().isClientSide){
            //根据速度造成伤害
            List<Entity> entities = level().getEntities(rider, rider.getBoundingBox());
            for (Entity entity : entities){
                if(entity.getBoundingBoxForCulling().contains(getPosition(1)))
                    entity.hurt(damageSources().playerAttack(rider), ((float) rider.getDeltaMovement().length() * 10));
            }
        }

        setPos(new Vec3(rider.getX(),rider.getY(),rider.getZ()));
        setYRot(rider.getYRot());

        rider.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
            if(!ssPlayer.isFlying()){
                ssPlayer.setHasSwordEntity(false);
                SwordSoaring.LOGGER.info("sword entity "+ getId() + " rider is not flying. "+level());
                discard();
            }
        });

    }

    /**
     * 判断范围内和自己有交叉的实体
     */
    public List<Entity> getHitEntities(double offset){
//        return level().getEntities(this, new AABB(getPosition(0).add(-5,-5,-5), getPosition(0).add(5,5,5))
//                , entity -> entity.getBoundingBox().contains(getPosition(0.5f)));
        return level().getEntities(this, new AABB(getPosition(0).add(-offset,-offset,-offset), getPosition(0).add(offset,offset,offset))
                , entity -> entity.getBoundingBox().intersects(getBoundingBox()));
    }

    /**
     * 调整姿势
     * @param poseStack 来自Renderer的render
     */
    @Override
    public void setPose(PoseStack poseStack){
        poseStack.mulPose(Axis.XP.rotationDegrees(90f));
        Item sword = getItemStack().getItem();
        if(sword instanceof UchigatanaItem || sword instanceof TachiItem || sword instanceof LongswordItem){
            poseStack.mulPose(Axis.ZP.rotationDegrees(45f + getYRot()));
        }else {
            poseStack.mulPose(Axis.ZP.rotationDegrees(-45f + getYRot()));
        }
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    protected void defineSynchedData() {
//        this.getEntityData().define(ITEM_STACK, ItemStack.EMPTY);
//        this.getEntityData().define(RIDER_UUID, Optional.empty());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.getEntityData().set(RIDER_UUID, Optional.of(tag.getUUID("rider_uuid")));
        this.getEntityData().set(ITEM_STACK, ItemStack.of(tag.getCompound("item_stack")));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if(rider != null){
            tag.putUUID("rider_uuid", this.getEntityData().get(RIDER_UUID).orElse(rider.getUUID()));
        }
        tag.put("item_stack", this.getEntityData().get(ITEM_STACK).serializeNBT());
    }

}
