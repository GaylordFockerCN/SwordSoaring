package net.p1nero.ss.network.packet.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.p1nero.ss.Config;
import net.p1nero.ss.capability.SSCapabilityProvider;
import net.p1nero.ss.entity.SwordConvergenceEntity;
import net.p1nero.ss.epicfight.animation.ModAnimations;
import net.p1nero.ss.epicfight.skill.ModSkills;
import net.p1nero.ss.epicfight.skill.SwordConvergence;
import net.p1nero.ss.epicfight.skill.SwordSoaringSkill;
import net.p1nero.ss.network.packet.BasePacket;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.skill.SkillDataManager;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import javax.annotation.Nullable;

import static net.p1nero.ss.epicfight.skill.SwordConvergence.TOTAL_SWORD_CNT;

/**
 * 实现开始飞行的服务端操作
 */
public record StartSwordConvergencePacket(boolean shouldRelease) implements BasePacket {
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(shouldRelease);
    }

    public static StartSwordConvergencePacket decode(FriendlyByteBuf buf) {
        return new StartSwordConvergencePacket(buf.readBoolean());
    }

    @Override
    public void execute(@Nullable Player player) {
        if (player != null && player.getServer() != null) {
            player.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).ifPresent(entityPatch -> {
                if(entityPatch instanceof ServerPlayerPatch serverPlayerPatch){
                    if(!shouldRelease){
                        if(!serverPlayerPatch.hasStamina(0.01f)){
                            return;
                        }
                        SkillDataManager dataManager = serverPlayerPatch.getSkill(ModSkills.SWORD_CONVERGENCE).getDataManager();
                        int totalSword = dataManager.getDataValue(TOTAL_SWORD_CNT);
                        SwordConvergence.summonSwords(serverPlayerPatch.getOriginal(), totalSword, 4 + totalSword/20);
                        if(!serverPlayerPatch.getOriginal().isCreative()){
                            serverPlayerPatch.consumeStamina(0.01f);
                        }
                        dataManager.setData(TOTAL_SWORD_CNT, totalSword + 1);
                    } else {
                        SwordConvergenceEntity.dir = player.getViewVector(1.0f);
                        SwordConvergenceEntity.finalTargetPos = player.getPosition(1.0f).add(player.getViewVector(1.0f).normalize().scale(5));
                        SwordConvergenceEntity.isShooting = true;
                    }
                }
            });
        }
    }
}