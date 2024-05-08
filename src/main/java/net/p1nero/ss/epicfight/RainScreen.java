package net.p1nero.ss.epicfight;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.p1nero.ss.SwordSoaring;
import net.p1nero.ss.capability.SSCapabilityProvider;
import net.p1nero.ss.entity.ModEntities;
import net.p1nero.ss.entity.RainScreenSwordEntity;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.skill.guard.GuardSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

import java.util.UUID;

import static net.p1nero.ss.util.ItemStackUtil.setLeftTick;

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
            SkillContainer guardSkill = event.getPlayerPatch().getSkill(SkillSlots.GUARD);
            if(guardSkill != null){
                summonRainScreen(event.getPlayerPatch().getOriginal(), event.getPlayerPatch());
            }
        });

    }

    private void summonRainScreen(ServerPlayer player, ServerPlayerPatch playerPatch){
        if(!SwordSoaring.isValidSword(player.getMainHandItem())){
            return;
        }
        player.getCapability(SSCapabilityProvider.SS_PLAYER).ifPresent(ssPlayer -> {
            if(ssPlayer.getSwordScreenEntityCount() == 0){
//                playerPatch.playAnimationSynchronized(Animations.DUMMY_ANIMATION, 0);
                for(int i = 0; i < 4; i++){
                    RainScreenSwordEntity sword = ModEntities.RAIN_SCREEN_SWORD.get().spawn(player.serverLevel(), player.getOnPos(), MobSpawnType.MOB_SUMMONED);
                    sword.setRider(player);
                    sword.setItemStack(player.getMainHandItem());
                    sword.setSwordID(i);
                    sword.setPos(player.getPosition(0.5f).add(sword.getOffset()));
                }
                ssPlayer.setSwordScreenEntityCount(4);
            }
        });
    }

    @Override
    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);
        PlayerEventListener listener = container.getExecuter().getEventListener();
        listener.removeListener(PlayerEventListener.EventType.SKILL_EXECUTE_EVENT, EVENT_UUID);
    }

    /**
     * 先领悟剑意，能御剑才能召唤雨帘
     * 哎呀前置能不能多放几个啊awa
     */
    @Override
    public Skill getPriorSkill() {
        return ModSkills.SWORD_SOARING;
    }
}
