package net.p1nero.ss.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.math.Axis;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.network.PlayMessages;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2d;
import yesman.epicfight.api.utils.math.Vec2f;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.world.item.LongswordItem;
import yesman.epicfight.world.item.TachiItem;
import yesman.epicfight.world.item.UchigatanaItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 滥用静态变量，我喜欢
 */
public class SwordConvergenceEntity extends AbstractArrow implements AbstractSwordEntity{

    public static List<Integer> list = new ArrayList<>();
    public static Vec3 finalTargetPos = Vec3.ZERO, dir = Vec3.ZERO;//所有的剑共用目标，所以静态
    public static int lastSwordTickCount;//记录最后一根剑的tick，
    public static boolean isShooting;//统一发射
    private Vec3 target0 = Vec3.ZERO;//从土里钻出来要聚集的地方
    private int shootingTick = 0, shootingCountDown = Integer.MAX_VALUE;
    private final double dis = 3 + random.nextInt(7), height = 3 + random.nextDouble();
    private boolean isFinalTargetPosPassed = false;
    private static final EntityDataAccessor<ItemStack> ITEM_STACK = SynchedEntityData.defineId(SwordConvergenceEntity.class, EntityDataSerializers.ITEM_STACK);

    public SwordConvergenceEntity(PlayMessages.SpawnEntity packet, Level world) {
        this(ModEntities.CONVERGENCE_SWORD.get(), world);
    }

    public SwordConvergenceEntity(EntityType<? extends AbstractArrow> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
        this.getEntityData().define(ITEM_STACK, ItemStack.EMPTY);
        list.add(this.getId());
    }

    public SwordConvergenceEntity(ItemStack itemStack, Level level, Vec3 target0) {
        this(ModEntities.CONVERGENCE_SWORD.get(), level);
        setItemStack(itemStack);
        this.target0 = target0;
    }

    public void setItemStack(ItemStack itemStack) {
        this.getEntityData().set(ITEM_STACK, itemStack);
    }

    @Override
    public ItemStack getItemStack() {
        return this.getEntityData().get(ITEM_STACK);
    }

    @Override
    public void setDeltaMovement(@NotNull Vec3 deltaMovement) {
        super.setDeltaMovement(deltaMovement);
    }

    /**
     * 更新最后一根剑的时间。
     * NOTE 写了但是没用上，但是能跑。。。。。算了不改了。。。。
     */
    public void updateLastTick(){
        lastSwordTickCount = tickCount;
    }

    @Override
    public void tick() {
        super.tick();

        double offsetY = 0.05 * Math.sin(tickCount * 0.1);
//        double offsetXZ = 0.5 * Math.sin(Math.toRadians(tickCount));
        double offsetXZ = 0.1 * Math.sin(tickCount * Math.PI);
        //还没飞到集合点就飞到集合点，飞到了就不动
        if(!isShooting){

            //懒得发包同步了
            if(target0 == Vec3.ZERO && getOwner() != null){
                target0 = getOwner().getPosition(1.0f);
            }

            //计算玩家视角和剑方向的夹角，如果在身后则抬高
            Vec2 ownerPosVec2 = Vec2.ZERO, ownerViewVec2 = Vec2.ZERO;
            if(getOwner() != null){
                ownerPosVec2 = new Vec2((float)(getX() - getOwner().getX()), (float)(getZ() - getOwner().getZ()));
                ownerViewVec2 = new Vec2(((float) getOwner().getViewVector(1.0f).x), ((float) getOwner().getViewVector(1.0f).z));
            }
            double dotProduct = ownerPosVec2.dot(ownerViewVec2);
            double cosTheta = dotProduct / (ownerPosVec2.length() * ownerViewVec2.length());
            double angleInRadians = Math.acos(cosTheta);

            //如果还没到玩家身边则飞向玩家
            if(getPosition(1.0f).distanceTo(target0) > dis){
                setDeltaMovement(target0.subtract(getPosition(1.0f)).normalize().add(0,offsetY * 5,0).scale(0.5f));
            } else {
                //如果到了但是在身后就抬高一点。夹角大于九十则视为在身后。
                //如果到指定位置了就停下并矫正角度（注意还没到不要矫正）。
                if(angleInRadians > Math.PI / 2 && getY() < target0.y + height){
                    setDeltaMovement(0,0.1,0);
                } else if(getY() > target0.y + height + 1){
                    setDeltaMovement(0,-0.1,0);
                } else {
                    setDeltaMovement(Vec3.ZERO);
                }
                if(getOwner()!=null){
                    setXRot(-getOwner().getXRot());
                    setYRot(-getOwner().getYRot());
                }
            }
        } else {
            //陆续发射，每次20根
            if(shootingCountDown / 20 > 1){
                if(shootingCountDown == Integer.MAX_VALUE){
                    shootingCountDown = tickCount - lastSwordTickCount;
                } else {
                    shootingCountDown--;
                }
                setYRot((float) Math.toDegrees(Math.atan2(dir.x, dir.z)));
                setXRot((float) Math.toDegrees(Math.atan2(dir.y, dir.horizontalDistance())));

                return;
            }

            //先射向目标，再随机抖动前进。
            shootingTick++;
//            double offsetXZ = 0;
            if(!isFinalTargetPosPassed){
                if(getPosition(1.0f).distanceTo(finalTargetPos) < 1){
                    isFinalTargetPosPassed = true;
                    level().playSound(null, getOnPos(), EpicFightSounds.ENTITY_MOVE.get(), SoundSource.BLOCKS, 1f, 1f);
                }
                if(finalTargetPos == Vec3.ZERO && getOwner() != null){
                    finalTargetPos = getOwner().getViewVector(1.0f).scale(5.0F);
                }
                setDeltaMovement(finalTargetPos.subtract(getPosition(1.0f)).normalize().add(offsetXZ, offsetY,offsetXZ));
            }else {
                if(dir == Vec3.ZERO && getOwner() != null){
                    dir = getOwner().getViewVector(1.0f);
                }
                setDeltaMovement(dir.normalize().add(offsetXZ,offsetY,offsetXZ));
            }

            //起飞一定时间后紫砂
            if(shootingTick > 100){
                list.remove((Integer)this.getId());
                if(SwordConvergenceEntity.list.isEmpty()){
                    SwordConvergenceEntity.isShooting = false;
                }
                discard();
            }
        }

        //保险
        if(tickCount > 142857){
            list.remove((Integer)this.getId());
            if(SwordConvergenceEntity.list.isEmpty()){
                SwordConvergenceEntity.isShooting = false;
            }
            discard();
        }

    }

    /**
     * 不自毁的版本
     */
    @Override
    protected void onHitEntity(@NotNull EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        Entity entity = entityHitResult.getEntity();
        if(getOwner() instanceof Player player && entity.getId() != player.getId()){
            entity.hurt(damageSources().playerAttack(player),0.5f);
            int fireLevel = getItemStack().getEnchantmentLevel(Enchantments.FIRE_ASPECT);
            if(fireLevel>0){
                //原版时间除以2
                entity.setSecondsOnFire(fireLevel * 2);
            }
            if(entity instanceof LivingEntity livingEntity){
                livingEntity.setHealth(livingEntity.getHealth() - 1);//强制扣血，防止霸体
            }
            level().playSound(null, getOnPos(), EpicFightSounds.BLADE_HIT.get(), SoundSource.BLOCKS,1f,1f);
            level().addParticle(EpicFightParticles.BLADE_RUSH_SKILL.get(), getX(),getY(),getZ(),0,0,0);
        }
    }

    /**
     * 穿透方块？
     */
    @Override
    protected void onHitBlock(@NotNull BlockHitResult hitResult) {}

    @Override
    protected @NotNull ItemStack getPickupItem() {
        return getItemStack();
    }

    /**
     * 跟RainCutter一样
     */
    @Override
    public void setPose(PoseStack poseStack) {

        Item sword = getItemStack().getItem();
        double pitchRad = Math.toRadians(-getYRot());
        double yawRad = Math.toRadians(-getXRot());
        float xRot = (float) Math.toDegrees(yawRad * Math.cos(pitchRad));
        float zRot = (float) Math.toDegrees(yawRad * Math.sin(pitchRad));
        if(0 < -getYRot() && -getYRot() <180){
            zRot = -zRot;
        }

        poseStack.mulPose(Axis.XP.rotationDegrees((xRot)));
        poseStack.mulPose(Axis.YP.rotationDegrees(getYRot() - 90 ));
        if(sword instanceof UchigatanaItem || sword instanceof TachiItem || sword instanceof LongswordItem){
            poseStack.mulPose(Axis.ZP.rotationDegrees(zRot - 45f));
        }else {
            poseStack.mulPose(Axis.ZP.rotationDegrees(zRot - 135f));
        }
    }

}
