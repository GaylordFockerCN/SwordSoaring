package net.p1nero.ss.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PlayMessages;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class SmoothDirSwordEntity extends RainCutterSwordEntity{

    private Vec3 pos0 = Vec3.ZERO, targetPos0 = Vec3.ZERO;
    private static final EntityDataAccessor<ItemStack> ITEM_STACK = SynchedEntityData.defineId(SmoothDirSwordEntity.class, EntityDataSerializers.ITEM_STACK);
    public SmoothDirSwordEntity(PlayMessages.SpawnEntity packet, Level world) {
        super(ModEntities.SMOOTH_DIR_SWORD.get(), world);
    }

    public SmoothDirSwordEntity(EntityType<? extends AbstractArrow> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
        this.getEntityData().define(ITEM_STACK, ItemStack.EMPTY);
    }

    public SmoothDirSwordEntity(ItemStack itemStack, Level level) {
        this(ModEntities.SMOOTH_DIR_SWORD.get(), level);
        setItemStack(itemStack);
    }

    public void setItemStack(ItemStack itemStack) {
        this.getEntityData().set(ITEM_STACK, itemStack);
    }

    @Override
    public ItemStack getItemStack() {
        if(this.getEntityData().hasItem(ITEM_STACK)){
            return this.getEntityData().get(ITEM_STACK);
        }else {
            return Items.DIAMOND_SWORD.getDefaultInstance();
        }
    }

    @Override
    public void updateDir() {
        if(getOwner() instanceof Player player){
            getOwner().getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).ifPresent(entityPatch -> {
                if(entityPatch instanceof PlayerPatch<?> playerPatch){
                    if(playerPatch.getTarget() == null){
                        if(targetPos0 == Vec3.ZERO){
                            dir = player.getViewVector(1.0f);
                        }else {
                            dir = getLerp(pos0, targetPos0);
                        }
                    } else {
                        LivingEntity target = playerPatch.getTarget();
                        //高一点比较好看，但是到头线怕碰不到
                        targetPos = target.getPosition(1.0f).add(0,target.getEyeHeight(),0);
                        if(targetPos0 == Vec3.ZERO){
                            targetPos0 = targetPos;
                        }
                        dir = getLerp(pos0, targetPos0);
                    }
                }
            });
        }
    }

    /**
     * 计算从初位置到末位置平滑过渡所需要的插值。
     */
    public Vec3 getLerp(Vec3 pos0, Vec3 targetPos){
        double fullDis = targetPos.subtract(pos0).length();
        double currentDis = getPosition(1.0f).subtract(pos0).length();
        return pos0.lerp(targetPos, currentDis/fullDis);
    }

    @Override
    public void initDirection() {
        pos0 = getPosition(1.0f);
        super.initDirection();
    }
}
