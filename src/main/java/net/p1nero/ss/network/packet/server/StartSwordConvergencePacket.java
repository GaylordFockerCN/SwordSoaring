package net.p1nero.ss.network.packet.server;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.p1nero.ss.Config;
import net.p1nero.ss.capability.SSCapabilityProvider;
import net.p1nero.ss.capability.SSPlayer;
import net.p1nero.ss.entity.SwordConvergenceEntity;
import net.p1nero.ss.epicfight.animation.ModAnimations;
import net.p1nero.ss.epicfight.skill.ModSkills;
import net.p1nero.ss.epicfight.skill.SwordConvergence;
import net.p1nero.ss.epicfight.skill.SwordSoaringSkill;
import net.p1nero.ss.network.packet.BasePacket;
import org.slf4j.spi.MDCAdapter;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.skill.SkillDataManager;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

import javax.annotation.Nullable;

//import static net.p1nero.ss.epicfight.skill.SwordConvergence.COOL_DOWN;
//import static net.p1nero.ss.epicfight.skill.SwordConvergence.TOTAL_SWORD_CNT;

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
            SSPlayer ssPlayer = player.getCapability(SSCapabilityProvider.SS_PLAYER).orElse(new SSPlayer());
            player.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).ifPresent(entityPatch -> {
                if(entityPatch instanceof ServerPlayerPatch serverPlayerPatch){
                    SkillDataManager dataManager = serverPlayerPatch.getSkill(ModSkills.SWORD_CONVERGENCE).getDataManager();
                    if(ssPlayer.swordConvCooldown > 0){
                        return;
                    }

                    if(!shouldRelease){
                        if(!serverPlayerPatch.hasStamina(0.01f)){
                            return;
                        }

                        int totalSword = ssPlayer.totalSwordCnt;
                        SwordConvergence.summonSwords(serverPlayerPatch.getOriginal(), totalSword, 4 + totalSword/20);
                        if(!serverPlayerPatch.getOriginal().isCreative()){
                            serverPlayerPatch.consumeForSkill(ModSkills.SWORD_CONVERGENCE, Skill.Resource.STAMINA, 0.01f);
                        }
                        ssPlayer.totalSwordCnt = ssPlayer.totalSwordCnt + 1;
//                        dataManager.setData(TOTAL_SWORD_CNT, totalSword + 1);
                    } else {
//                        dataManager.setDataSync(TOTAL_SWORD_CNT, 0, serverPlayerPatch.getOriginal());//FIXME 不起作用
                        ssPlayer.swordConvCooldown = Config.SWORD_CONVERGENCE_COOLDOWN.get().intValue() + ssPlayer.swordConvCooldown;
//                        dataManager.setData(COOL_DOWN, Config.SWORD_CONVERGENCE_COOLDOWN.get().intValue()+dataManager.getDataValue(TOTAL_SWORD_CNT));
                        ssPlayer.totalSwordCnt = 0;
//                        dataManager.setData(TOTAL_SWORD_CNT, 0);
//                        dataManager.setDataSync(COOL_DOWN, Config.SWORD_CONVERGENCE_COOLDOWN.get().intValue(), serverPlayerPatch.getOriginal());
                        if(serverPlayerPatch.getTarget() != null){
                            SwordConvergenceEntity.dir = serverPlayerPatch.getTarget().getPosition(1.0f).subtract(player.getPosition(1.0f)).normalize();
                            SwordConvergenceEntity.finalTargetPos = player.getPosition(1.0f).add(SwordConvergenceEntity.dir.scale(7));
                        } else {
                            SwordConvergenceEntity.finalTargetPos = player.getPosition(1.0f).add(player.getViewVector(1.0f).normalize().scale(7));
                            SwordConvergenceEntity.dir = player.getViewVector(1.0f);
                        }
                        SwordConvergenceEntity.isShooting = true;

                    }

                }

            });
        }
    }
}