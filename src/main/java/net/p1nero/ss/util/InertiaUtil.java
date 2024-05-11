package net.p1nero.ss.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.p1nero.ss.SwordSoaring;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Predicate;

/**
 * 控制速度向量存储的运算
 */
public class InertiaUtil {

    /**
     * 最长记录向量的时间
     */
    public static final int maxRecordTick = 100;

    /**
     * 获取剩余惯性移动刻
     */
    public static int getLeftTick(CompoundTag sword) {
        return sword.getInt("leftTick");
    }

    /**
     * 设置剩余惯性移动刻
     */
    public static void setLeftTick(CompoundTag sword, int leftTick) {
        if(leftTick<0){
            return;
        }
        sword.putInt("leftTick", Math.min(leftTick, maxRecordTick));
    }

    /**
     * 获取末速度的向量
     */
    public static Vec3 getEndVec(CompoundTag tag) {
        return new Vec3(tag.getDouble("endX"),tag.getDouble("endY"),tag.getDouble("endZ"));
    }

    /**
     * 设置末速度的向量
     */
    public static void setEndVec(CompoundTag tag, Vec3 endVec) {
        tag.putDouble("endX", endVec.x);
        tag.putDouble("endY", endVec.y);
        tag.putDouble("endZ", endVec.z);
    }

    /**
     * 获取前n个tick前的方向向量
     * 懒得重写了，直接用Player的persistentData吧
     */
    public static Vec3 getViewVec(CompoundTag sword, int tickBefore){
        if(tickBefore > maxRecordTick){
            return Vec3.ZERO;
        }
        checkOrCreateTag(sword);
        return getQueue(sword).toArray(new Vec3[maxRecordTick])[maxRecordTick -tickBefore];
    }

    /**
     * 保存很多个tick前的方向向量，实现惯性效果
     * 通过队列来保存。
     * 并作插值，实现惯性漂移（太妙了）
     */
    public static void updateViewVec(CompoundTag sword, Vec3 viewVec){
        checkOrCreateTag(sword);
        Queue<Vec3> tickValues = getQueue(sword);
        tickValues.add(viewVec);
        Vec3 old = tickValues.poll();
        Queue<Vec3> newTickValues = new ArrayDeque<>();
        for(double i = 1; i <= tickValues.size(); i++){
            Vec3 newVec3 = old.lerp(viewVec, i / tickValues.size());
            newTickValues.add(newVec3);
        }
        saveQueue(sword, newTickValues);
    }

    /**
     * 获取前几个tick内的方向向量队列
     */
    public static Queue<Vec3> getQueue(CompoundTag sword){
        CompoundTag tag = checkOrCreateTag(sword);
        Queue<Vec3> tickValues = new ArrayDeque<>();
        for(int i = 0; i < maxRecordTick; i++){
            CompoundTag tickVec = tag.getList("view_vec_queue", Tag.TAG_COMPOUND).getCompound(i);
            tickValues.add(new Vec3(tickVec.getDouble("x"),tickVec.getDouble("y"),tickVec.getDouble("z")));
        }
        return tickValues;
    }

    /**
     * 保存前几个tick内的方向向量队列
     */
    public static void saveQueue(CompoundTag sword, Queue<Vec3> tickValues){
        CompoundTag tag = checkOrCreateTag(sword);
        for(int i = 0; i < maxRecordTick; i++){
            CompoundTag tickVecTag = tag.getList("view_vec_queue", Tag.TAG_COMPOUND).getCompound(i);
            Vec3 tickVec = tickValues.remove();
            tickVecTag.putDouble("x", tickVec.x);
            tickVecTag.putDouble("y", tickVec.y);
            tickVecTag.putDouble("z", tickVec.z);
        }
    }

    /**
     * 检查是否为空标签，是则创建一个完备的给它。防止异常。
     */
    public static CompoundTag checkOrCreateTag(CompoundTag tag){
        if (!tag.contains("view_vec_queue")) {
            ListTag tickTagsList = new ListTag();
            for (int i = 0; i < maxRecordTick; i++) {
                tickTagsList.add(new CompoundTag());
            }
            tag.put("view_vec_queue", tickTagsList);
        }
        return tag;
    }

}
