package net.p1nero.ss.epicfight.skill;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.AABB;
import net.p1nero.ss.SwordSoaring;
import net.p1nero.ss.capability.SSCapabilityProvider;
import net.p1nero.ss.capability.SSPlayer;
import net.p1nero.ss.entity.ModEntities;
import net.p1nero.ss.entity.RainScreenSwordEntity;
import net.p1nero.ss.epicfight.animation.ModAnimations;
import net.p1nero.ss.network.PacketHandler;
import net.p1nero.ss.network.PacketRelay;
import net.p1nero.ss.network.packet.SyncSwordOwnerPacket;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
            if(event.getPlayerPatch().hasStamina(10)){
                //播放动画，对周围实体造成伤害
                ServerPlayer player =  event.getPlayerPatch().getOriginal();
                SSPlayer ssPlayer = player.getCapability(SSCapabilityProvider.SS_PLAYER).orElse(new SSPlayer());
                if(!event.getPlayerPatch().getEntityState().inaction() && ssPlayer.getSwordScreensID().isEmpty()){
                    event.getPlayerPatch().playAnimationSynchronized(ModAnimations.RAIN_SCREEN, 0);
                    double r = 3;
                    List<Entity> entities = player.serverLevel().getEntities(player,new AABB(player.getPosition(0).add(-r,-r,-r), player.getPosition(0).add(r,r,r)));
                    for (Entity entity : entities){
                        if(entity.getId() == player.getId()){
                            continue;
                        }
                        entity.hurt(player.damageSources().playerAttack(player), 2f);
                    }
                }

            }
        });

        listener.addEventListener(PlayerEventListener.EventType.HURT_EVENT_PRE, EVENT_UUID, (event) -> {
            DamageSource damageSource = event.getDamageSource();
            float knockback = 0.25F;
            SSPlayer ssPlayer = event.getPlayerPatch().getOriginal().getCapability(SSCapabilityProvider.SS_PLAYER).orElse(new SSPlayer());
            ServerPlayer serverPlayer = event.getPlayerPatch().getOriginal();
            if(ssPlayer.getSwordScreenEntityCount() > 0 && !ssPlayer.isProtectNextFall()){

                Set<Integer> swordID = ssPlayer.getSwordScreensID();
                //剑挡伤害并回血（顺序很重要，需要先销毁剑再反弹，省得获取SourceEntity为null的时候被打断）
                for(int i : swordID){
                    Entity sword = serverPlayer.serverLevel().getEntity(i);
                    if(sword != null){
                        event.getPlayerPatch().consumeStaminaAlways(3);
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

    }

    public static void summonSword(ServerPlayer player){
        if(!SwordSoaring.isValidSword(player.getMainHandItem())){
            return;
        }
        player.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {

            //多个保险，以防重启的时候剑帘不存在。其实可以设置不读取即可（
            Set<Integer> swordID = ssPlayer.getSwordScreensID();
//            if(swordID.isEmpty()){
//                ssPlayer.setSwordScreenEntityCount(0);
//            }
//            for(Integer entityID : swordID){
//                if(!((player.serverLevel().getEntity(entityID) instanceof RainScreenSwordEntity))){
//                    ssPlayer.setSwordScreenEntityCount(0);
//                    ssPlayer.setSwordID(new HashSet<>());
//                    break;
//                }
//            }
//
//            if(ssPlayer.getSwordScreenEntityCount() == 0){
//
//                for(int i = 0; i < 4; i++){
//                    RainScreenSwordEntity sword = ModEntities.RAIN_SCREEN_SWORD.get().spawn(player.serverLevel(), player.getOnPos(), MobSpawnType.MOB_SUMMONED);
//                    if(sword == null){
//                        return;
//                    }
//                    sword.setRider(player);
//                    sword.setItemStack(player.getMainHandItem());
//                    PacketRelay.sendToAll(PacketHandler.INSTANCE, new SyncSwordOwnerPacket(player.getId(), sword.getId()));
//                    sword.setSwordID(i);
//                    sword.setPos(player.getPosition(0.5f).add(sword.getOffset()));
//                    ssPlayer.setSwordScreenEntityCount(ssPlayer.getSwordScreenEntityCount()+1);
//                    swordID.add(sword.getId());
//                }
//
//            }

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

    @Override
    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);
        PlayerEventListener listener = container.getExecuter().getEventListener();
        listener.removeListener(PlayerEventListener.EventType.HURT_EVENT_PRE, EVENT_UUID);
        listener.removeListener(PlayerEventListener.EventType.SERVER_ITEM_USE_EVENT, EVENT_UUID);
    }

}
