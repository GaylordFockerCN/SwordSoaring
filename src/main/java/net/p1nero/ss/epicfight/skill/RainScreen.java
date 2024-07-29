package net.p1nero.ss.epicfight.skill;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.p1nero.ss.SwordSoaring;
import net.p1nero.ss.capability.SSCapabilityProvider;
import net.p1nero.ss.capability.SSPlayer;
import net.p1nero.ss.entity.ModEntities;
import net.p1nero.ss.entity.RainScreenSwordEntity;
import net.p1nero.ss.epicfight.animation.ModAnimations;
import net.p1nero.ss.network.PacketHandler;
import net.p1nero.ss.network.PacketRelay;
import net.p1nero.ss.network.packet.client.AddBladeRushSkillParticlePacket;
import net.p1nero.ss.network.packet.client.SyncSwordOwnerPacket;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 画雨笼山
 * 所有带Data的都得重写。。。
 */
public class RainScreen extends Skill {

    private static final UUID EVENT_UUID = UUID.fromString("051a9bb2-7541-11ee-b962-0242ac114515");

    public RainScreen(Builder<? extends Skill> builder) {
        super(builder);
    }

    /**
     * 监听是否在防御状态
     */
    @Override
    public void onInitiate(SkillContainer container) {

        PlayerEventListener listener = container.getExecuter().getEventListener();

        listener.addEventListener(PlayerEventListener.EventType.SERVER_ITEM_USE_EVENT, EVENT_UUID, (event) -> {
            ServerPlayerPatch caster = event.getPlayerPatch();
            if(caster.hasStamina(10)){

                ServerPlayer player =  caster.getOriginal();
                SSPlayer ssPlayer = player.getCapability(SSCapabilityProvider.SS_PLAYER).orElse(new SSPlayer());

                if(!SwordSoaring.isValidSword(player.getMainHandItem()) || caster.getEntityState().inaction()
                        || !ssPlayer.getSwordScreensID().isEmpty() || !caster.isBattleMode() || ssPlayer.rainScreenCooldownTimer > 0){
                    return;
                }

                //播放动画，对周围实体造成伤害
                caster.playAnimationSynchronized(ModAnimations.RAIN_SCREEN, 0);
                double r = 2;//伤害范围
                List<Entity> entities = player.serverLevel().getEntities(player,new AABB(player.getPosition(0).add(-r,-r,-r), player.getPosition(0).add(r,r,r)));
                for (Entity entity : entities){
                    if(entity.getId() == player.getId()){
                        continue;
                    }
                    entity.hurt(player.damageSources().playerAttack(player), 2f);

                    //如果是正面的实体则播放特效和音效
                    Vec3 targetLocation = entity.getPosition(1.0f);
                    Vec3 viewVector = player.getViewVector(1.0F);
                    viewVector = viewVector.subtract(0.0, viewVector.y, 0.0).normalize();
                    Vec3 toSourceLocation = targetLocation.subtract(caster.getOriginal().position()).normalize();
                    if (toSourceLocation.dot(viewVector) > 0.0) {
                            PacketRelay.sendToPlayer(PacketHandler.INSTANCE, new AddBladeRushSkillParticlePacket(targetLocation, Vec3.ZERO), player);
                            player.level().playSound(null, entity.getOnPos(), EpicFightSounds.BLADE_HIT.get(), SoundSource.BLOCKS,1f,1f);
                    }

                }

            }
        });

        listener.addEventListener(PlayerEventListener.EventType.HURT_EVENT_PRE, EVENT_UUID, (event) -> {
            DamageSource damageSource = event.getDamageSource();
            float knockback = 0.25F;
            SSPlayer ssPlayer = event.getPlayerPatch().getOriginal().getCapability(SSCapabilityProvider.SS_PLAYER).orElse(new SSPlayer());
            ServerPlayer serverPlayer = event.getPlayerPatch().getOriginal();
            if(ssPlayer.getSwordScreenEntityCount() > 0 && !(ssPlayer.isProtectNextFall() && damageSource.is(DamageTypeTags.IS_FALL))){

                Set<Integer> swordID = ssPlayer.getSwordScreensID();
                //剑挡伤害并回血（顺序很重要，需要先销毁剑再反弹，省得获取SourceEntity为null的时候被打断）
                for(int i : swordID){
                    Entity sword = serverPlayer.serverLevel().getEntity(i);
                    if(sword != null){
                        event.getPlayerPatch().consumeForSkill(this, Resource.STAMINA, 3);
                        serverPlayer.heal(2f);
//                        serverPlayer.serverLevel().addParticle(ParticleTypes.HAPPY_VILLAGER, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),0,0,0);
//                        serverPlayer.serverLevel().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1, 1);
                        ssPlayer.setSwordScreenEntityCount(ssPlayer.getSwordScreenEntityCount()-1);
                        sword.discard();
                        swordID.remove(i);
                        ssPlayer.setSwordID(swordID);
                        event.setCanceled(true);
                        event.setResult(AttackResult.ResultType.BLOCKED);
                        break;
                    }
                }

                event.getPlayerPatch().playSound(EpicFightSounds.CLASH.get(), -0.05F, 0.1F);
                EpicFightParticles.HIT_BLUNT.get().spawnParticleWithArgument(serverPlayer.serverLevel(), HitParticleType.FRONT_OF_EYES, HitParticleType.ZERO, serverPlayer, damageSource.getDirectEntity());

                if (damageSource.getDirectEntity() instanceof LivingEntity livingEntity) {
                    knockback += EnchantmentHelper.getKnockbackBonus(livingEntity) * 0.1F;
                    event.getPlayerPatch().knockBackEntity(damageSource.getDirectEntity().position(), knockback);
                }

            }
        });

        listener.addEventListener(PlayerEventListener.EventType.SKILL_CONSUME_EVENT, EVENT_UUID, (event) -> {
            if (event.getSkill() instanceof RainScreen && !container.getExecuter().getOriginal().isCreative() && container.getStack() > 0) {
                this.setStackSynchronize((ServerPlayerPatch)container.getExecuter(), container.getStack() - 1);

                event.setResourceType(Resource.NONE);
            }

        });

    }

    public static void summonSword(ServerPlayer player){

        player.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {

            //多个保险，以防重启的时候剑帘不存在。其实可以设置不读取即可（
            Set<Integer> swordID = ssPlayer.getSwordScreensID();

            for(int i = 0; i < 4; i++){
                RainScreenSwordEntity sword = ModEntities.RAIN_SCREEN_SWORD.get().spawn(player.serverLevel(), player.getOnPos(), MobSpawnType.MOB_SUMMONED);
                if(sword == null){
                    return;
                }
                sword.setRider(player);
                sword.setItemStack(player.getMainHandItem());
                PacketRelay.sendToAll(PacketHandler.INSTANCE, new SyncSwordOwnerPacket(player.getId(), sword.getId()));
                sword.setSwordID(i);
                sword.setPos(player.getPosition(0.5f).add(sword.getOffset()));
                ssPlayer.setSwordScreenEntityCount(ssPlayer.getSwordScreenEntityCount()+1);
                swordID.add(sword.getId());
            }

        });
    }

    public static void onPlayerTick(TickEvent.PlayerTickEvent event){
        event.player.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
            if(ssPlayer.rainScreenCooldownTimer >0){
                ssPlayer.rainScreenCooldownTimer--;
            }
        });
    }

    @Override
    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);
        PlayerEventListener listener = container.getExecuter().getEventListener();
        listener.removeListener(PlayerEventListener.EventType.HURT_EVENT_PRE, EVENT_UUID);
        listener.removeListener(PlayerEventListener.EventType.SERVER_ITEM_USE_EVENT, EVENT_UUID);
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean shouldDraw(SkillContainer container) {
        AtomicBoolean toReturn = new AtomicBoolean(false);
        container.getExecuter().getOriginal().getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
            toReturn.set(ssPlayer.rainScreenCooldownTimer > 0);
        });
        return toReturn.get();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y) {
        Player player = container.getExecuter().getOriginal();
        SSPlayer ssPlayer = player.getCapability(SSCapabilityProvider.SS_PLAYER).orElse(new SSPlayer());
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(0, (float)gui.getSlidingProgression(), 0);
        guiGraphics.blit(ModSkills.RAIN_SCREEN.getSkillTexture(), (int)x, (int)y, 24, 24, 0, 0, 1, 1, 1, 1);
        guiGraphics.drawString(gui.font, String.format("%d", (ssPlayer.rainScreenCooldownTimer /40)), x + 6, y + 6, 16777215, true);
        poseStack.popPose();
    }

}
