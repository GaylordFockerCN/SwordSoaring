package net.p1nero.ss.keymapping;

import net.minecraft.client.KeyMapping;

public class MyKeyMapping extends KeyMapping {
    boolean isRelease;
    boolean lock;
    boolean isEvenNumber;
    public MyKeyMapping(String p_90821_, int p_90822_, String p_90823_) {
        super(p_90821_, p_90822_, p_90823_);
    }

    @Override
    public void setDown(boolean down) {
        super.setDown(down);
        if(down){
            lock = true;
            isRelease = false;
        }else if(lock){
            lock = false;
            isRelease = true;
            isEvenNumber = !isEvenNumber;
        }
    }

    /**
     * 判断是否松开并重置
     */
    public boolean isRelease() {
        if(isRelease){
            isRelease = false;
            return true;
        }
        return false;
    }

    /**
     * 是否是偶数次按下
     */
    public boolean isEvenNumber() {
        return isEvenNumber;
    }
}
