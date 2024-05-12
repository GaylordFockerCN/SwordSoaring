package net.p1nero.ss.epicfight;

import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.p1nero.ss.capability.SSCapabilityProvider;
import net.p1nero.ss.network.PacketHandler;
import net.p1nero.ss.network.PacketRelay;
import net.p1nero.ss.network.packet.StartYakshaJumpPacket;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.utils.LevelUtil;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSkills;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.skill.*;
import yesman.epicfight.skill.passive.PassiveSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.BiFunction;

/**
 * 靖妖傩舞!
 * 本质是无CD的毁坏跳跃
 */
public class YakshaMask extends PassiveSkill {

    private static final UUID EVENT_UUID = UUID.fromString("051a9bb2-7541-11ee-b962-0242ac114517");

    protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, StaticAnimation>> slamMotions;

    //决定高度
    public static final int height = 5;
    //低头的角度
    public static final float forcedXRot = 89.9f;

    /**
     * 抄流星猛击的
     * TODO 做自己的动画 make custom animation
     */
    public static YakshaMask.Builder createYakshaMaskBuilder() {
        return (new YakshaMask.Builder()).setCategory(SkillCategories.IDENTITY).setResource(Resource.NONE)
                .addSlamMotion(CapabilityItem.WeaponCategories.SPEAR, (item, player) -> Animations.METEOR_SLAM)
                .addSlamMotion(CapabilityItem.WeaponCategories.GREATSWORD, (item, player) -> Animations.METEOR_SLAM)
                .addSlamMotion(CapabilityItem.WeaponCategories.TACHI, (item, player) -> Animations.METEOR_SLAM)
                .addSlamMotion(CapabilityItem.WeaponCategories.LONGSWORD, (item, player) -> Animations.METEOR_SLAM);
    }

    public YakshaMask(Builder builder) {
        super(builder);
        slamMotions = builder.slamMotions;
    }


    /**
     * 起跳的部分修改了 {@link yesman.epicfight.skill.mover.DemolitionLeapSkill#castSkill}
     */
    @Override
    public void onInitiate(SkillContainer container) {
        PlayerEventListener listener = container.getExecuter().getEventListener();
        listener.addEventListener(PlayerEventListener.EventType.MOVEMENT_INPUT_EVENT, EVENT_UUID, (event) -> {
            LocalPlayerPatch localPlayerPatch = event.getPlayerPatch();
            Player player = localPlayerPatch.getOriginal();
            boolean jumpPressed = Minecraft.getInstance().options.keyJump.isDown();
            player.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {

                //player.onGround()此时已经是false，所以不能在这里判断
                if (!jumpPressed || player.getVehicle() != null || player.getAbilities().flying || !localPlayerPatch.isBattleMode()
                        || ssPlayer.getYakshaMaskTimer() == 0 || !ssPlayer.canYakshaMask) {
                    return;
                }
                //直接由这个来判断能否起跳，并且在tick事件中对其修改（已经懒得保存能否跳了）
                ssPlayer.canYakshaMask = false;
                int height = YakshaMask.height + new Random().nextInt(3);
                PacketRelay.sendToServer(PacketHandler.INSTANCE, new StartYakshaJumpPacket(height));
                int modifiedTicks = (int)(7.466800212860107 * Math.log10((float)height + 1.0F) / Math.log10(2.0));
                Vec3f jumpDirection = new Vec3f(0.0F, (float)modifiedTicks * 0.05F, 0.0F);
                float xRot = Mth.clamp(70.0F + Mth.clamp(-60f, -90.0F, 0.0F), 0.0F, 70.0F);
                jumpDirection.add(0.0F, xRot / 70.0F * 0.05F, 0.0F);
                jumpDirection.rotate(xRot, Vec3f.X_AXIS);
                jumpDirection.rotate(-localPlayerPatch.getCameraYRot(), Vec3f.Y_AXIS);
                localPlayerPatch.getOriginal().setDeltaMovement(jumpDirection.toDoubleVector());

            });

        });

        //监听技能，释放技能后进入夜叉傩面，如果在空中则下落攻击
        listener.addEventListener(PlayerEventListener.EventType.SKILL_EXECUTE_EVENT, EVENT_UUID, (event) -> {
            Skill skill = event.getSkillContainer().getSkill();
            Player player = event.getPlayerPatch().getOriginal();
            player.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
                //客户端找不到方法限制。。按G就可以启动。。能放技能的三把武器太那啥了
                if(skill.getCategory() == SkillCategories.WEAPON_INNATE){
                    ssPlayer.setYakshaMaskTimer(400);
                    player.level().playSound(null, player.getOnPos(), SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 0.3f,1f);
                }

                //下面为流星猛击源码的修改版，取消了使用限制，如果不符合则强制低头
                if (skill.getCategory() != SkillCategories.BASIC_ATTACK && skill.getCategory() != SkillCategories.AIR_ATTACK) {
                    return;
                }

                //canYakshaMask是为了判断是否出于技能时间内
                if (player.onGround() || ssPlayer.canYakshaMask) {
                    return;
                }

                if(!event.getPlayerPatch().hasStamina(1.5f)){
                    return;
                }

                //保留原有动画，不然只有剑有动画
                StaticAnimation slamAnimation;
                CapabilityItem holdingItem = container.getExecuter().getHoldingItemCapability(InteractionHand.MAIN_HAND);
                if (!this.slamMotions.containsKey(holdingItem.getWeaponCategory())) {
                    slamAnimation = Animations.METEOR_SLAM;
                } else {
                    slamAnimation = this.slamMotions.get(holdingItem.getWeaponCategory()).apply(holdingItem, container.getExecuter());
                    if (slamAnimation == null) {
                        slamAnimation = Animations.METEOR_SLAM;
                    }
                }

                Vec3 vec3 = player.getEyePosition(1.0F);
                Vec3 vec31 = player.getViewVector(1.0F);
                Vec3 vec32 = vec3.add(vec31.x * 50.0, vec31.y * 50.0, vec31.z * 50.0);
                HitResult hitResult = player.level().clip(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, container.getExecuter().getOriginal()));
                if (hitResult.getType() != HitResult.Type.MISS) {
                    event.getPlayerPatch().consumeStamina(1.5f);
                    Vec3 to = hitResult.getLocation();
                    Vec3 from = player.position();
                    double distance = to.distanceTo(from);
                    if (distance > 6.0) {
                        ssPlayer.isYakshaFall = true;
                        container.getExecuter().playAnimationSynchronized(slamAnimation, 0.0F);
                        event.setCanceled(true);
                    }
                } else {
                    //把头低下！
                    player.setXRot(forcedXRot);
                    vec31 = player.getViewVector(1.0F);
                    vec32 = vec3.add(vec31.x * 50.0, vec31.y * 50.0, vec31.z * 50.0);
                    hitResult = player.level().clip(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, container.getExecuter().getOriginal()));
                    if (hitResult.getType() != HitResult.Type.MISS) {
                        event.getPlayerPatch().consumeStamina(1.5f);
                        Vec3 to = hitResult.getLocation();
                        Vec3 from = player.position();
                        double distance = to.distanceTo(from);
                        if (distance > 6.0) {
                            ssPlayer.isYakshaFall = true;
                            container.getExecuter().playAnimationSynchronized(slamAnimation, 0.0F);
                            event.setCanceled(true);
                        }
                    }
                }

            });
        });

        //免疫摔落伤害
        listener.addEventListener(PlayerEventListener.EventType.HURT_EVENT_PRE, EVENT_UUID, (event) -> {
            if (event.getDamageSource().is(DamageTypeTags.IS_FALL) ) {
                Player player = event.getPlayerPatch().getOriginal();
                player.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
                    if(ssPlayer.isProtectNextFall()){
                        event.setAmount(0.0F);
                        event.setCanceled(true);
                        ssPlayer.setProtectNextFall(false);
                    }
                });
            }
        });

        //播放下落砸地特效
        listener.addEventListener(PlayerEventListener.EventType.FALL_EVENT, EVENT_UUID, (event) -> {
            Player player = event.getPlayerPatch().getOriginal();
            player.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
                if(ssPlayer.isYakshaFall){
                    ssPlayer.isYakshaFall = false;
                    LevelUtil.circleSlamFracture(null, player.level(), player.position().subtract(0.0, 1.0, 0.0), height * 10 * 0.05, false, false, true);
                }
            });
        });

    }

    @Override
    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);
        container.getExecuter().getEventListener().removeListener(PlayerEventListener.EventType.MOVEMENT_INPUT_EVENT, EVENT_UUID);
        container.getExecuter().getEventListener().removeListener(PlayerEventListener.EventType.HURT_EVENT_PRE, EVENT_UUID);
    }

    /**
     * 控制技能持续时间和冷却
     */
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        player.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
            int yakshaMaskTimer = ssPlayer.getYakshaMaskTimer();
            if(yakshaMaskTimer > 0){
                ssPlayer.setYakshaMaskTimer(yakshaMaskTimer-1);
                player.level().addParticle(EpicFightParticles.BOSS_CASTING.get(), player.getX(),player.getY()+1,player.getZ(),0,0.2,0);
            }
            if(event.player.onGround()){
                ssPlayer.canYakshaMask = true;
            }
        });
    }

    public static class Builder extends Skill.Builder<YakshaMask> {
        protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, StaticAnimation>> slamMotions = Maps.newHashMap();

        public Builder() {
        }

        public YakshaMask.Builder addSlamMotion(WeaponCategory weaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, StaticAnimation> function) {
            this.slamMotions.put(weaponCategory, function);
            return this;
        }

        public YakshaMask.Builder setCategory(SkillCategory category) {
            this.category = category;
            return this;
        }

        public YakshaMask.Builder setActivateType(Skill.ActivateType activateType) {
            this.activateType = activateType;
            return this;
        }

        public YakshaMask.Builder setResource(Skill.Resource resource) {
            this.resource = resource;
            return this;
        }

        public YakshaMask.Builder setCreativeTab(CreativeModeTab tab) {
            this.tab = tab;
            return this;
        }
    }

}
