package net.p1nero.ss.epicfight.skill;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.p1nero.ss.Config;
import net.p1nero.ss.SwordSoaring;
import net.p1nero.ss.capability.SSCapabilityProvider;
import net.p1nero.ss.capability.SSPlayer;
import net.p1nero.ss.entity.StellarSwordEntity;
import net.p1nero.ss.entity.SwordConvergenceEntity;
import net.p1nero.ss.epicfight.animation.ModAnimations;
import net.p1nero.ss.network.PacketHandler;
import net.p1nero.ss.network.PacketRelay;
import net.p1nero.ss.network.packet.server.StartSwordConvergencePacket;
import net.p1nero.ss.util.SkillDataUtil;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.skill.*;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

import java.util.Random;
import java.util.UUID;

/**
 * 万剑归宗！
 */
public class SwordConvergence extends Skill {

    public static final SkillDataKey<Boolean> IS_PRESSING = SkillDataKey.createBooleanKey(false, false, SwordConvergence.class);
    public static final SkillDataKey<Integer> TOTAL_SWORD_CNT = SkillDataKey.createIntKey(0,false, SwordConvergence.class);
    public static final SkillDataKey<Integer> COOL_DOWN = SkillDataKey.createIntKey(0,false, SwordConvergence.class);
    private static final UUID EVENT_UUID = UUID.fromString("051a9bb2-7541-11ee-b962-0242ac114519");
    public SwordConvergence(Builder<? extends Skill> builder) {
        super(builder);
    }

    @Override
    public void onInitiate(SkillContainer container) {
        SkillDataUtil.registerSkillData(container, IS_PRESSING, TOTAL_SWORD_CNT, COOL_DOWN);
    }

    public static void onPlayerTick(TickEvent.PlayerTickEvent event){
        Player player = event.player;
        //太超模，仅限创造
        if(!player.isCreative()){
            return;
        }
        if(!player.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).isPresent() || !SwordSoaring.isValidSword(player.getMainHandItem())){
            return;
        }

        PlayerPatch<?> patch = (PlayerPatch<?>)player.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);

        if(patch.getSkill(ModSkills.SWORD_CONVERGENCE) == null || patch.getSkill(ModSkills.SWORD_CONVERGENCE).isEmpty() || !patch.isBattleMode()){
            return;
        }
        SkillDataManager dataManager = patch.getSkill(ModSkills.SWORD_CONVERGENCE).getDataManager();

        int cooldown = dataManager.getDataValue(COOL_DOWN);
        if(cooldown > 0){
            dataManager.setData(COOL_DOWN, cooldown-1);
            return;
        } else {
            SwordConvergenceEntity.isShooting = false;
        }

        if(player.isLocalPlayer()){
            if(EpicFightKeyMappings.LOCK_ON.isDown()){

                patch.playAnimationSynchronized(ModAnimations.STELLAR_RESTORATION_PRE0, 0.0F);
                player.setYBodyRot(player.getYHeadRot());//不这样的话头动身不动
                player.setDeltaMovement(Vec3.ZERO);

                dataManager.setData(IS_PRESSING, true);
                PacketRelay.sendToServer(PacketHandler.INSTANCE, new StartSwordConvergencePacket(false));
                int totalSword = dataManager.getDataValue(TOTAL_SWORD_CNT);
                dataManager.setData(TOTAL_SWORD_CNT, totalSword + 1);
            }else if(dataManager.getDataValue(IS_PRESSING)){

                patch.playAnimationSynchronized(ModAnimations.STELLAR_RESTORATION_PRE,0);

                dataManager.setData(IS_PRESSING, false);
                dataManager.setData(COOL_DOWN, Config.SWORD_CONVERGENCE_COOLDOWN.get().intValue()+dataManager.getDataValue(TOTAL_SWORD_CNT));
                dataManager.setData(TOTAL_SWORD_CNT, 0);
                PacketRelay.sendToServer(PacketHandler.INSTANCE, new StartSwordConvergencePacket(true));
                if(patch.getTarget() != null){
                    SwordConvergenceEntity.dir = patch.getTarget().getPosition(1.0f).subtract(player.getPosition(1.0f)).normalize();
                    SwordConvergenceEntity.finalTargetPos = player.getPosition(1.0f).add(SwordConvergenceEntity.dir.scale(7));
                } else {
                    SwordConvergenceEntity.finalTargetPos = player.getPosition(1.0f).add(player.getViewVector(1.0f).normalize().scale(7));
                    SwordConvergenceEntity.dir = player.getViewVector(1.0f);
                }
                SwordConvergenceEntity.isShooting = true;
            }
        }

    }

    public static void summonSwords(ServerPlayer player, int swordCnt, int r){
        if(!SwordSoaring.isValidSword(player.getMainHandItem())){
            return;
        }
        Random random = new Random();

        double angle = random.nextDouble() * Math.PI / 2; // 生成0到90度之间的角度
        double radius = 10 + Math.abs(random.nextDouble()) * r; // 生成半径。开局远一点比较好看
        double y = 2 + Math.abs(random.nextDouble()) * r / 4;
        int type = swordCnt % 4;
        double x = switch (type){
            case 1, 2 -> -radius * Math.cos(angle);
            default -> radius * Math.cos(angle);
        };
        double z = switch (type){
            case 0, 1 -> radius * Math.sin(angle);
            default -> -radius * Math.sin(angle);
        };
        summonSword(player, player.getX()+x,player.getY()+y,player.getZ()+z);

    }

    /**
     * 召唤单根剑
     * @param player 玩家位置
     * @param x x 偏移
     * @param y y 偏移
     * @param z z 偏移
     */
    public static void summonSword(ServerPlayer player, double x, double y, double z, double targetX, double targetY, double targetZ){
        if(!SwordSoaring.isValidSword(player.getMainHandItem())){
            return;
        }
        SwordConvergenceEntity sword = new SwordConvergenceEntity(player.getMainHandItem(), player.level(), new Vec3(targetX,targetY,targetZ));
        sword.setOwner(player);
        sword.setNoGravity(true);
        sword.setBaseDamage(0);
        sword.setSilent(false);
        sword.pickup = AbstractArrow.Pickup.DISALLOWED;
        sword.setKnockback(0);//击退
        sword.setPierceLevel((byte) 5);//穿透
        sword.setPos(x,y,z);
        sword.setDeltaMovement(new Vec3(targetX,targetY,targetZ).subtract(sword.getPosition(1.0f)).normalize());
        player.level().playSound(null, sword.getOnPos(), EpicFightSounds.ENTITY_MOVE.get(), SoundSource.BLOCKS, 0.1f,0.5f);
        player.serverLevel().addFreshEntity(sword);

    }

    /**
     * 召唤单根剑
     * @param player 玩家位置
     * @param x x 偏移
     * @param y y 偏移
     * @param z z 偏移
     */
    public static void summonSword(ServerPlayer player, double x, double y, double z){
        summonSword(player, x, y, z, player.getX(),player.getY(),player.getZ());
    }

    @Override
    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);
        SkillDataUtil.removeSkillData(container, IS_PRESSING, TOTAL_SWORD_CNT, COOL_DOWN);
        container.getExecuter().getEventListener().removeListener(PlayerEventListener.EventType.CLIENT_ITEM_USE_EVENT, EVENT_UUID);
        container.getExecuter().getEventListener().removeListener(PlayerEventListener.EventType.SERVER_ITEM_USE_EVENT, EVENT_UUID);
        container.getExecuter().getEventListener().removeListener(PlayerEventListener.EventType.SERVER_ITEM_STOP_EVENT, EVENT_UUID);
    }

    @Override
    public boolean shouldDraw(SkillContainer container) {
        return container.getDataManager().getDataValue(COOL_DOWN) > 0;
    }

    @Override
    public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y) {
        if(!container.getDataManager().hasData(COOL_DOWN)){
            return;
        }
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(0, (float)gui.getSlidingProgression(), 0);
        guiGraphics.blit(ModSkills.SWORD_CONVERGENCE.getSkillTexture(), (int)x, (int)y, 24, 24, 0, 0, 1, 1, 1, 1);
        guiGraphics.drawString(gui.font, String.format("%.1f", (container.getDataManager().getDataValue(COOL_DOWN) / 40.0f)), x + 3, y + 6, 16777215, true);
        poseStack.popPose();
    }
}
