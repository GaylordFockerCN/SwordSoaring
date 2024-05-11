package net.p1nero.ss.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import net.p1nero.ss.network.PacketHandler;
import net.p1nero.ss.network.PacketRelay;
import net.p1nero.ss.network.packet.AddBladeRushSkillParticlePacket;
import net.p1nero.ss.network.packet.AddSmokeParticlePacket;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.item.LongswordItem;
import yesman.epicfight.world.item.TachiItem;
import yesman.epicfight.world.item.UchigatanaItem;

import java.util.Objects;

public class RainCutterSwordEntity extends AbstractArrow implements AbstractSwordEntity{
    public static final float speed = 1;
    private Vec3 targetPos = Vec3.ZERO;
    private Vec3 dir = Vec3.ZERO;

    private static final EntityDataAccessor<ItemStack> ITEM_STACK = SynchedEntityData.defineId(RainCutterSwordEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> RAIN_CUTTER_SWORD_ID = SynchedEntityData.defineId(RainCutterSwordEntity.class, EntityDataSerializers.INT);

    public RainCutterSwordEntity(PlayMessages.SpawnEntity packet, Level world) {
        this(ModEntities.RAIN_CUTTER_SWORD.get(), world);
    }

    public RainCutterSwordEntity(EntityType<? extends AbstractArrow> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
        this.getEntityData().define(ITEM_STACK, ItemStack.EMPTY);
        this.getEntityData().define(RAIN_CUTTER_SWORD_ID, -1);
    }

    public RainCutterSwordEntity(ItemStack itemStack, Level level, int swordID) {
        this(ModEntities.RAIN_CUTTER_SWORD.get(), level);
        setSwordID(swordID);
        setItemStack(itemStack);
    }

    public void setItemStack(ItemStack itemStack) {
        this.getEntityData().set(ITEM_STACK, itemStack);
    }

    @Override
    public ItemStack getItemStack() {
        return this.getEntityData().get(ITEM_STACK);
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public void setSwordID(int swordID){
        getEntityData().set(RAIN_CUTTER_SWORD_ID, swordID);
    }

    public int getRainCutterSwordId() {
        return getEntityData().get(RAIN_CUTTER_SWORD_ID);
    }

    /**
     * 必须先setOwner，根据Owner的位置来决定偏移
     */
    public Vec3 getOffset(){
        double dis = 1.3;
        double yRot = Math.toRadians(Objects.requireNonNull(getOwner()).getYRot());//虽然短时间内应该不会变吧
        return switch (getRainCutterSwordId()){
            case 0 -> new Vec3(-dis*Math.cos(yRot),2.5, -dis*Math.sin(yRot));
            case 1 -> new Vec3(0,3,0);
            case 2 -> new Vec3(dis*Math.cos(yRot),2.5,dis*Math.sin(yRot));
            default -> new Vec3(0,0,0);
        };
    }

    /**
     * 改了AbstractArrow的tick，把调整旋转和不能访问的地方删了
     */
    @Override
    public void tick() {

//-----------------------------------------------------以下是原版剑的修改（吐槽一下，感觉写得一坨屎 →_→）-------------------------------------------------
        boolean flag = this.isNoPhysics();
        Vec3 vec3 = this.getDeltaMovement();

        BlockPos blockpos = this.blockPosition();
        BlockState blockstate = this.level().getBlockState(blockpos);
        Vec3 vec33;
        if (!blockstate.isAir() && !flag) {
            VoxelShape voxelshape = blockstate.getCollisionShape(this.level(), blockpos);
            if (!voxelshape.isEmpty()) {
                vec33 = this.position();

                for (AABB aabb : voxelshape.toAabbs()) {
                    if (aabb.move(blockpos).contains(vec33)) {
                        this.inGround = true;
                        break;
                    }
                }
            }
        }

        if (this.shakeTime > 0) {
            --this.shakeTime;
        }

        if (this.isInWaterOrRain() || blockstate.is(Blocks.POWDER_SNOW) || this.isInFluidType((fluidType, height) -> this.canFluidExtinguish(fluidType))) {
            this.clearFire();
        }

        if (this.inGround && !flag) {

            ++this.inGroundTime;
        } else {
            this.inGroundTime = 0;
            Vec3 vec32 = this.position();
            vec33 = vec32.add(vec3);
            HitResult hitresult = this.level().clip(new ClipContext(vec32, vec33, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (((HitResult)hitresult).getType() != HitResult.Type.MISS) {
                vec33 = ((HitResult)hitresult).getLocation();
            }

            while(!this.isRemoved()) {
                EntityHitResult entityhitresult = this.findHitEntity(vec32, vec33);
                if (entityhitresult != null) {
                    hitresult = entityhitresult;
                }

                if (hitresult != null && ((HitResult)hitresult).getType() == HitResult.Type.ENTITY) {
                    Entity entity = ((EntityHitResult)hitresult).getEntity();
                    Entity entity1 = this.getOwner();
                    if (entity instanceof Player && entity1 instanceof Player && !((Player)entity1).canHarmPlayer((Player)entity)) {
                        hitresult = null;
                        entityhitresult = null;
                    }
                }

                if (hitresult != null && ((HitResult)hitresult).getType() != HitResult.Type.MISS && !flag) {
                    switch (ForgeEventFactory.onProjectileImpactResult(this, (HitResult)hitresult)) {
                        case SKIP_ENTITY:
                            if (((HitResult)hitresult).getType() != HitResult.Type.ENTITY) {
                                this.onHit((HitResult)hitresult);
                                this.hasImpulse = true;
                            } else {
                                entityhitresult = null;
                            }
                            break;
                        case STOP_AT_CURRENT_NO_DAMAGE:
                            this.discard();
                            entityhitresult = null;
                            break;
                        case STOP_AT_CURRENT:
                            this.setPierceLevel((byte)0);
                        case DEFAULT:
                            this.onHit((HitResult)hitresult);
                            this.hasImpulse = true;
                    }
                }

                if (entityhitresult == null || this.getPierceLevel() <= 0) {
                    break;
                }

                hitresult = null;
            }

            if (this.isRemoved()) {
                return;
            }

            vec3 = this.getDeltaMovement();
            double d5 = vec3.x;
            double d6 = vec3.y;
            double d1 = vec3.z;
            if (this.isCritArrow()) {
                for(int i = 0; i < 4; ++i) {
                    this.level().addParticle(ParticleTypes.CRIT, this.getX() + d5 * (double)i / 4.0, this.getY() + d6 * (double)i / 4.0, this.getZ() + d1 * (double)i / 4.0, -d5, -d6 + 0.2, -d1);
                }
            }

            double d7 = this.getX() + d5;
            double d2 = this.getY() + d6;
            double d3 = this.getZ() + d1;
            float f = 0.99F;
            if (this.isInWater()) {
                for(int j = 0; j < 4; ++j) {
                    this.level().addParticle(ParticleTypes.BUBBLE, d7 - d5 * 0.25, d2 - d6 * 0.25, d3 - d1 * 0.25, d5, d6, d1);
                }

                f = this.getWaterInertia();
            }

            this.setDeltaMovement(vec3.scale(f));
            if (!this.isNoGravity() && !flag) {
                Vec3 vec34 = this.getDeltaMovement();
                this.setDeltaMovement(vec34.x, vec34.y - 0.05000000074505806, vec34.z);
            }

            this.setPos(d7, d2, d3);
            this.checkInsideBlocks();
        }
//-----------------------------------------------------以上是原版箭的修改-------------------------------------------------

        if(firstTick){
            this.level().addParticle(EpicFightParticles.AIR_BURST.get(), this.getX() , this.getY() , this.getZ() , 0,0,0);
            firstTick = false;
        }

        updateDir();
        setDeltaMovement(dir.normalize().scale(speed));
        //NOTE! Client改变方向会让剑扭来扭去的，但是客户端也得setDeltaMovement
        if(!level().isClientSide){
            setYRot((float) Math.toDegrees(Math.atan2(dir.x, dir.z)));
            this.setXRot((float) Math.toDegrees(Math.atan2(dir.y, dir.horizontalDistance())));
        }
        //存活太久或者落地后一小会儿要紫砂。不落地马上紫砂是为了看清轨迹，以及更好的位置反馈
        if(tickCount > 100 || inGroundTime > 2){
            PacketRelay.sendToAll(PacketHandler.INSTANCE, new AddSmokeParticlePacket(getPosition(1.0f) ,getDeltaMovement()));
            discard();
        }

    }

    /**
     * 更新方向，存在目标就朝向目标，不存在目标就玩家看的方向
     */
    public void updateDir(){
        if(getOwner() instanceof Player player){
            getOwner().getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).ifPresent(entityPatch -> {
                if(entityPatch instanceof PlayerPatch<?> playerPatch){
                    if(playerPatch.getTarget() == null){
                        dir = player.getViewVector(1.0f);
                    } else {
                        LivingEntity target = playerPatch.getTarget();
                        //高一点比较好看，但是到头线怕碰不到
                        targetPos = target.getPosition(1.0f).add(0,target.getEyeHeight(),0);
                        dir = targetPos.subtract(getPosition(1.0f));
                    }
                }
            });
        }
    }

    /**
     * 调整初始发射方向
     */
    public void initDirection(){
        updateDir();
        setDeltaMovement(dir.normalize().scale(speed));
        setYRot((float) Math.toDegrees(Math.atan2(dir.x, dir.z)));
        setXRot((float) Math.toDegrees(Math.atan2(dir.y, dir.horizontalDistance())));
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        Entity entity = entityHitResult.getEntity();
        if(getOwner() instanceof ServerPlayer player){
            if(entity.getId() != player.getId()){
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
                PacketRelay.sendToAll(PacketHandler.INSTANCE, new AddBladeRushSkillParticlePacket(getPosition(1.0f) ,getDeltaMovement()));
                discard();
            }
        }
        super.onHitEntity(entityHitResult);
    }

    @Override
    protected @NotNull ItemStack getPickupItem() {
        return getItemStack();
    }

    /**
     * 加了个Pitch要了我的老命。。。试了6小时估计。
     * 终于想到斜着的时候的旋转合着就是两个方向的旋转叠加
     * 然后再用一下三角函数和脑子
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
