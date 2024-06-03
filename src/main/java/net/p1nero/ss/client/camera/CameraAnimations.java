package net.p1nero.ss.client.camera;

import net.minecraft.resources.ResourceLocation;
import net.p1nero.ss.SwordSoaring;

public class CameraAnimations {
    public static CameraAnimation KEQING_BURST;

    public static void loadAnimations(){
        KEQING_BURST = CameraAnimation.load(new ResourceLocation(SwordSoaring.MOD_ID, "camera_animation/keqing_burst.json"));
    }

}
