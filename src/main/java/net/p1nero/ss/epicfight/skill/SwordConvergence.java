package net.p1nero.ss.epicfight.skill;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.p1nero.ss.Config;
import net.p1nero.ss.SwordSoaring;
import net.p1nero.ss.capability.SSCapabilityProvider;
import net.p1nero.ss.entity.RainCutterSwordEntity;
import net.p1nero.ss.entity.SmoothDirSwordEntity;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

import java.util.Random;
import java.util.UUID;

/**
 * 万剑归宗！
 */
public class SwordConvergence extends Skill {

    private static final UUID EVENT_UUID = UUID.fromString("051a9bb2-7541-11ee-b962-0242ac114519");
    public SwordConvergence(Builder<? extends Skill> builder) {
        super(builder);
    }

    @Override
    public void onInitiate(SkillContainer container) {

        PlayerEventListener listener = container.getExecuter().getEventListener();

        listener.addEventListener(PlayerEventListener.EventType.SKILL_EXECUTE_EVENT, EVENT_UUID, (event) -> {
            Skill skill = event.getSkillContainer().getSkill();
            Player player = event.getPlayerPatch().getOriginal();
            if(skill.getCategory() == SkillCategories.WEAPON_INNATE && player instanceof ServerPlayer serverPlayer){
                summonSwords(serverPlayer, 1000);
            }
        });

    }

    public static void summonSwords(ServerPlayer player, int swordCnt){
        if(!SwordSoaring.isValidSword(player.getMainHandItem())){
            return;
        }
        int y = 5; // 总共生成的点的数量
        Random random = new Random();

        for (int i = 0; i < swordCnt / 4; i++) {
            double angle = random.nextDouble() * Math.PI / 2; // 生成0到90度之间的角度
            double radius = random.nextDouble() * 4 * swordCnt/300.0; // 生成半径

            // 第一象限
            double x1 = radius * Math.cos(angle);
            double z1 = radius * Math.sin(angle);
            summonSword(player, x1, y + 2 * random.nextDouble(), z1);

            // 第二象限
            double x2 = -radius * Math.cos(angle);
            double z2 = radius * Math.sin(angle);
            summonSword(player, x2, y + 2 * random.nextDouble(), z2);

            // 第三象限
            double x3 = -radius * Math.cos(angle);
            double z3 = -radius * Math.sin(angle);
            summonSword(player, x3, y + 2 * random.nextDouble(), z3);

            // 第四象限
            double x4 = radius * Math.cos(angle);
            double z4 = -radius * Math.sin(angle);
            summonSword(player, x4, y + 2 * random.nextDouble(), z4);
        }

    }

    /**
     * 召唤单根剑
     * @param player 玩家位置
     * @param x x 偏移
     * @param y y 偏移
     * @param z z 偏移
     */
    public static void summonSword(ServerPlayer player, double x, double y, double z){
//        SmoothDirSwordEntity sword = new SmoothDirSwordEntity(player.getMainHandItem(), player.level());
//        sword.setOwner(player);
//        sword.setNoGravity(true);
//        sword.setBaseDamage(0.01);
//        sword.setSilent(true);
//        sword.pickup = AbstractArrow.Pickup.DISALLOWED;
//        sword.setKnockback(1);//击退
//        sword.setPierceLevel((byte) 5);//穿透
//        sword.setPos(player.getPosition(1.0f).add(x,y,z));
//        sword.initDirection();
//        player.level().playSound(null, sword.getOnPos(), EpicFightSounds.ENTITY_MOVE.get(), SoundSource.BLOCKS, 0.1f,1);
//        player.serverLevel().addFreshEntity(sword);

        RainCutterSwordEntity sword = new RainCutterSwordEntity(player.getMainHandItem(), player.level(), 1);
        sword.setOwner(player);
        sword.setNoGravity(true);
        sword.setBaseDamage(0.01);
        sword.setSilent(true);
        sword.pickup =AbstractArrow.Pickup.DISALLOWED;
        sword.setKnockback(1);//击退
        sword.setPierceLevel((byte) 5);//穿透
        sword.setPos(player.getPosition(1.0f).add(x,y,z));
        sword.initDirection();
        player.level().playSound(null, sword.getOnPos(), EpicFightSounds.ENTITY_MOVE.get(), SoundSource.BLOCKS, 0.3f,1);
        player.serverLevel().addFreshEntity(sword);

    }

    @Override
    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);
        PlayerEventListener listener = container.getExecuter().getEventListener();
        listener.removeListener(PlayerEventListener.EventType.SKILL_EXECUTE_EVENT, EVENT_UUID);
    }

}
