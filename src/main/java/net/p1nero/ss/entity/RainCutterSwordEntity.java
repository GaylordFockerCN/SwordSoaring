package net.p1nero.ss.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.p1nero.ss.SwordSoaring;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.item.LongswordItem;
import yesman.epicfight.world.item.TachiItem;
import yesman.epicfight.world.item.UchigatanaItem;

import java.util.Optional;
import java.util.UUID;

public class RainCutterSwordEntity extends SwordEntity{

    private boolean startChase = false;

    private Vec3 targetPos = Vec3.ZERO;
    private Vec3 dir = Vec3.ZERO;
    private Vec3 pos0 = Vec3.ZERO;

    private static final EntityDataAccessor<Optional<UUID>> RIDER_UUID = SynchedEntityData.defineId(RainCutterSwordEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> TARGET_ID = SynchedEntityData.defineId(RainCutterSwordEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<ItemStack> ITEM_STACK = SynchedEntityData.defineId(RainCutterSwordEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> RAIN_CUTTER_SWORD_ID = SynchedEntityData.defineId(RainCutterSwordEntity.class, EntityDataSerializers.INT);

    public RainCutterSwordEntity(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
        this.getEntityData().define(ITEM_STACK, ItemStack.EMPTY);
        this.getEntityData().define(RIDER_UUID, Optional.empty());
        this.getEntityData().define(TARGET_ID, -1);
        this.getEntityData().define(RAIN_CUTTER_SWORD_ID, -1);
    }

    public RainCutterSwordEntity(ItemStack itemStack, Player rider, LivingEntity target, int swordID) {
        super(ModEntities.RAIN_CUTTER_SWORD.get(), rider.level());
        this.rider = rider;
        this.getEntityData().define(ITEM_STACK, itemStack);
        this.getEntityData().define(TARGET_ID, target.getId());
        this.getEntityData().define(RIDER_UUID, Optional.of(rider.getUUID()));
        this.getEntityData().define(RAIN_CUTTER_SWORD_ID, swordID);
    }

    @Override
    public ItemStack getItemStack() {
        return this.getEntityData().get(ITEM_STACK);
    }

    public void setItemStack(ItemStack itemStack) {
        this.getEntityData().set(ITEM_STACK, itemStack);
    }

    public void setTarget(LivingEntity target){
        this.getEntityData().set(TARGET_ID, target.getId());
    }

    public void setRider(Player rider) {
        this.rider = rider;
        this.getEntityData().set(RIDER_UUID, Optional.of(rider.getUUID()));
    }

    public void setSwordID(int swordID){
        getEntityData().set(RAIN_CUTTER_SWORD_ID, swordID);
    }

    public int getRainCutterSwordId() {
        return getEntityData().get(RAIN_CUTTER_SWORD_ID);
    }

    public void startChase() {
        this.startChase = true;
        pos0 = getPosition(1.0f);
    }

    public LivingEntity getTarget() {
        return ((LivingEntity) level().getEntity(getEntityData().get(TARGET_ID)));
    }

    public Vec3 getOffset(){
        double dis = 1.3;
        double yRot = Math.toRadians(getYRot());//虽然短时间内应该不会变吧
        return switch (getRainCutterSwordId()){
            case 0 -> new Vec3(-dis*Math.cos(yRot),2.5, -dis*Math.sin(yRot));
            case 1 -> new Vec3(0,3,-dis*Math.sin(yRot));
            case 2 -> new Vec3(dis*Math.cos(yRot),2.5,-dis*Math.sin(yRot));
            default -> new Vec3(0,0,0);
        };
    }

    @Override
    public void tick() {

        if(!startChase){
            return;
        }

        //想办法不让rider为null
        if(rider == null){
            if(this.getEntityData().get(RIDER_UUID).isPresent()){
                rider = level().getPlayerByUUID(this.getEntityData().get(RIDER_UUID).get());
            }else {
                SwordSoaring.LOGGER.info("sword entity "+ getId() + " doesn't have rider "+level());
                discard();
                return;
            }
        }

        if(rider == null){
            SwordSoaring.LOGGER.info("sword entity "+ getId() + " doesn't have rider "+level());
            discard();
            return;
        }

        //5tick更新一次目标方向
        if(tickCount % 5 == 0){
            rider.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).ifPresent(entityPatch -> {
                if(entityPatch instanceof PlayerPatch<?> playerPatch){
                    if(playerPatch.getTarget() != null){
                        LivingEntity target = playerPatch.getTarget();
                        //高一点比较好看，但是到头线怕碰不到
                        targetPos = target.getPosition(1.0f).add(0,target.getEyeHeight() *2 / 3,0);
                        dir = targetPos.subtract(getPosition(1.0f));
                    }
                }
            });
        }

        if(targetPos != Vec3.ZERO){
//            //统一比例
            dir = dir.scale(1.0f/ dir.length());
//            //向target飞去
//            setDeltaMovement(targetPos);//不管用，所以setPos...
//            setPos(vec0.lerp(targetPos,tickCount / 100.0));
            Vec3 moveTo = targetPos.subtract(pos0);
            moveTo = moveTo.scale((tickCount * 1.0 / 3.0) / moveTo.length());
            setPos(pos0.add(moveTo));
            setYRot(-(float) Math.toDegrees(Math.atan2(dir.x, dir.z)));
        }else {
//            double yR = Math.toRadians(getYRot());
//            setPos(getPosition(1.0f).add(Math.cos(yR),0,Math.sin(yR)));
            setYRot(rider.getYRot());
        }

        for(Entity entity : getHitEntities()){
            if(entity.getId() != rider.getId()){
                entity.hurt(damageSources().playerAttack(rider),0.5f);
                if(entity instanceof LivingEntity livingEntity){
                    livingEntity.setHealth(livingEntity.getHealth() - 1);//强制扣血，防止霸体
                }
                discard();
                return;
            }
        }

        //存活太久也要紫砂
        if(tickCount > 100){
            discard();
        }

    }

    public void setPose(PoseStack poseStack) {
        poseStack.mulPose(Axis.XP.rotationDegrees(90f));
        Item sword = getItemStack().getItem();
        if(SwordSoaring.epicFightLoad() && (sword instanceof UchigatanaItem || sword instanceof TachiItem || sword instanceof LongswordItem)){
            poseStack.mulPose(Axis.ZP.rotationDegrees(45f + getYRot()));
        }else {
            poseStack.mulPose(Axis.ZP.rotationDegrees(-45f + getYRot()));
        }
    }
}
