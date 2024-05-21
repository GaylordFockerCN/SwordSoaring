package net.p1nero.ss.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PlayMessages;
import net.p1nero.ss.capability.SSCapabilityProvider;
import net.p1nero.ss.network.PacketHandler;
import net.p1nero.ss.network.PacketRelay;
import net.p1nero.ss.network.packet.client.AddBladeRushSkillParticlePacket;
import net.p1nero.ss.network.packet.client.AddSmokeParticlePacket;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.world.item.LongswordItem;
import yesman.epicfight.world.item.TachiItem;
import yesman.epicfight.world.item.UchigatanaItem;

/**
 * 本来想继承自RainCutterSwordEntity的，但是区别太多了干脆重复一下。
 */
public class StellarSwordEntity extends AbstractArrow implements AbstractSwordEntity{

    private boolean inEntity;
    private Vec3 movement0;
    private static final EntityDataAccessor<ItemStack> ITEM_STACK = SynchedEntityData.defineId(StellarSwordEntity.class, EntityDataSerializers.ITEM_STACK);

    public StellarSwordEntity(PlayMessages.SpawnEntity packet, Level world) {
        this(ModEntities.STELLAR_SWORD.get(), world);
    }

    public StellarSwordEntity(EntityType<? extends AbstractArrow> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
        this.getEntityData().define(ITEM_STACK, ItemStack.EMPTY);
    }

    public StellarSwordEntity(ItemStack itemStack, Level level) {
        this(ModEntities.STELLAR_SWORD.get(), level);
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
    public void setDeltaMovement(@NotNull Vec3 deltaMovement) {
        super.setDeltaMovement(deltaMovement);
        movement0 = deltaMovement;
    }

    @Override
    public void tick() {
        super.tick();

        //客户端才生效！
        if(tickCount == 0){
            this.level().addParticle(EpicFightParticles.AIR_BURST.get(), this.getX() , this.getY() , this.getZ() , 0,0,0);
        }

        if(tickCount > 3){
            setDeltaMovement(movement0.scale(0.01));
        }

        if(tickCount > 100){
            if(getOwner() instanceof ServerPlayer serverPlayer){
                serverPlayer.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
                    ssPlayer.isStellarRestorationPressing = false;
                });
                PacketRelay.sendToAll(PacketHandler.INSTANCE, new AddSmokeParticlePacket(getPosition(1.0f) ,getDeltaMovement()));
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
        if(inEntity){
            return;
        }
        Entity entity = entityHitResult.getEntity();
        if(getOwner() instanceof ServerPlayer player){
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
        }
        setDeltaMovement(movement0.scale(0.01));
        inEntity = true;
    }

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
