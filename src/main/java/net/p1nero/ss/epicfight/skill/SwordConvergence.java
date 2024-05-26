package net.p1nero.ss.epicfight.skill;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.p1nero.ss.SwordSoaring;
import net.p1nero.ss.entity.StellarSwordEntity;
import net.p1nero.ss.entity.SwordConvergenceEntity;
import net.p1nero.ss.network.PacketHandler;
import net.p1nero.ss.network.PacketRelay;
import net.p1nero.ss.network.packet.server.StartSwordConvergencePacket;
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
    private static final UUID EVENT_UUID = UUID.fromString("051a9bb2-7541-11ee-b962-0242ac114519");
    public SwordConvergence(Builder<? extends Skill> builder) {
        super(builder);
    }

    @Override
    public void onInitiate(SkillContainer container) {
        container.getDataManager().registerData(IS_PRESSING);
        container.getDataManager().registerData(TOTAL_SWORD_CNT);
    }

    public static void onPlayerTick(TickEvent.PlayerTickEvent event){
        Player player = event.player;

        if(!player.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).isPresent()){
            return;
        }

        PlayerPatch<?> patch = (PlayerPatch<?>)player.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);
        if(patch.getSkill(ModSkills.SWORD_CONVERGENCE).isEmpty()){
            return;
        }
        if(player.isLocalPlayer()){
            SkillDataManager dataManager = patch.getSkill(ModSkills.SWORD_CONVERGENCE).getDataManager();
            if(EpicFightKeyMappings.LOCK_ON.isDown()){
                System.out.println("down");
                dataManager.setData(IS_PRESSING, true);
                PacketRelay.sendToServer(PacketHandler.INSTANCE, new StartSwordConvergencePacket(false));
            }else if(dataManager.getDataValue(IS_PRESSING)){
                dataManager.setData(IS_PRESSING, false);
                PacketRelay.sendToServer(PacketHandler.INSTANCE, new StartSwordConvergencePacket(true));
                SwordConvergenceEntity.dir = player.getViewVector(1.0f);
                SwordConvergenceEntity.finalTargetPos = player.getPosition(1.0f).add(player.getViewVector(1.0f).normalize().scale(5));
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
        double radius = random.nextDouble() * r; // 生成半径
        double y = 5 + random.nextDouble() * r / 4;
        int type = swordCnt % 4;
        double x = switch (type){
            case 1, 2 -> -radius * Math.cos(angle);
            default -> radius * Math.cos(angle);
        };
        double z = switch (type){
            case 0, 1 -> radius * Math.sin(angle);
            default -> -radius * Math.sin(angle);
        };

        summonSword(player, x,y,z, -x,-y,-z);

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
        SwordConvergenceEntity sword = new SwordConvergenceEntity(player.getMainHandItem(), player.level(), new Vec3(targetX, targetY, targetZ));
        sword.setOwner(player);
        sword.setNoGravity(true);
        sword.setBaseDamage(0.01);
        sword.setSilent(false);
        sword.pickup = AbstractArrow.Pickup.DISALLOWED;
        sword.setKnockback(0);//击退
        sword.setPierceLevel((byte) 5);//穿透
        sword.setPos(x,y,z);
        player.level().playSound(null, sword.getOnPos(), EpicFightSounds.ENTITY_MOVE.get(), SoundSource.BLOCKS, 1,1);
        player.serverLevel().addFreshEntity(sword);
    }

    @Override
    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);
        container.getDataManager().setData(IS_PRESSING, false);
        container.getExecuter().getEventListener().removeListener(PlayerEventListener.EventType.CLIENT_ITEM_USE_EVENT, EVENT_UUID);
        container.getExecuter().getEventListener().removeListener(PlayerEventListener.EventType.SERVER_ITEM_USE_EVENT, EVENT_UUID);
        container.getExecuter().getEventListener().removeListener(PlayerEventListener.EventType.SERVER_ITEM_STOP_EVENT, EVENT_UUID);
    }

}
