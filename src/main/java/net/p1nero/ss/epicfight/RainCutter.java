package net.p1nero.ss.epicfight;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraftforge.event.TickEvent;
import net.p1nero.ss.SwordSoaring;
import net.p1nero.ss.capability.SSCapabilityProvider;
import net.p1nero.ss.entity.RainCutterSwordEntity;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

import java.util.UUID;

/**
 * 裁雨留虹
 */

public class RainCutter extends Skill {


    private static final UUID EVENT_UUID = UUID.fromString("051a9bb2-7541-11ee-b962-0242ac114516");

    public RainCutter(Builder<? extends Skill> builder) {
        super(builder);
    }

    /**
     * 监听是否在防御状态
     */
    @Override
    public void onInitiate(SkillContainer container) {

        PlayerEventListener listener = container.getExecuter().getEventListener();

        listener.addEventListener(PlayerEventListener.EventType.SKILL_EXECUTE_EVENT, EVENT_UUID, (event) -> {
            Skill skill = event.getSkillContainer().getSkill();
            Player player = event.getPlayerPatch().getOriginal();
            player.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
                if(skill.getCategory() == SkillCategories.WEAPON_INNATE){
                    ssPlayer.setRainCutterTimer(400);//很奇怪200tick不是10s吗怎么实测5s？？被迫加长时间
                    ssPlayer.setScreenCutterCoolDown(false);
                }else if(skill.getCategory() == SkillCategories.BASIC_ATTACK || skill.getCategory() == SkillCategories.AIR_ATTACK){
                    if(ssPlayer.getRainCutterTimer() > 0 && !ssPlayer.isScreenCutterCoolDown() /*&& event.getPlayerPatch().getTarget() != null*/){
                        ssPlayer.setScreenCutterCoolDown(true);
                        if(player instanceof ServerPlayer serverPlayer){
                            summonSword(serverPlayer);
                        }
                    }
                }
            });
        });

    }

    public void summonSword(ServerPlayer player){
        if(!SwordSoaring.isValidSword(player.getMainHandItem())){
            return;
        }
        for(int i = 0; i < 3; i++){
            RainCutterSwordEntity sword = new RainCutterSwordEntity(player.getMainHandItem(), player.level(), i);
            sword.setOwner(player);
            sword.setNoGravity(true);
            sword.setBaseDamage(0.01);
            sword.setSilent(true);
            sword.pickup =AbstractArrow.Pickup.DISALLOWED;
            sword.setKnockback(1);//击退
            sword.setPierceLevel((byte) 5);//穿透
            sword.setPos(player.getPosition(1.0f).add(sword.getOffset()));
            sword.initDirection();
            player.serverLevel().addFreshEntity(sword);
        }
    }

    /**
     * 控制技能持续时间和冷却
     */
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {

        event.player.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
            int rainCutterTimer = ssPlayer.getRainCutterTimer();
            if(rainCutterTimer > 0){
                ssPlayer.setRainCutterTimer(rainCutterTimer-1);
            }
            //每秒刷新一次，一秒只能A出一次技能 (很奇怪20tick不是1s吗怎么实测5s？？被迫加长时间
            if(rainCutterTimer % 40 == 0 && rainCutterTimer != 0){
                ssPlayer.setScreenCutterCoolDown(false);
            }
        });

    }

    @Override
    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);
        PlayerEventListener listener = container.getExecuter().getEventListener();
        listener.removeListener(PlayerEventListener.EventType.SKILL_EXECUTE_EVENT, EVENT_UUID);
    }

}
