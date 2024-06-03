package net.p1nero.ss.client.camera;

import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.p1nero.ss.SwordSoaring;

/**
 * <a href="https://github.com/dfdyz/epicacg-1.18/blob/main/src/main/java/com/dfdyz/epicacg/event/CameraEvents.java">1.18迁移,有几个方法变了。感谢东非大野猪大大开源!</a>
 */
@OnlyIn(value = Dist.CLIENT)
@Mod.EventBusSubscriber(modid = SwordSoaring.MOD_ID, value = Dist.CLIENT)
public class CameraAnimationManager {
    private static float yawLock = 0f;
    private static Vec3 posLock = new Vec3(0,0,0);
    public static CameraAnimation currentAnim;
    private static int tick = 0;
    private static int linkTick = 0;
    private static int maxLinkTick = 3;
    private static boolean isEnd = true;
    private static boolean linking = false;
    private static LivingEntity orginal;
    private static final Vec3 Vec3UP = new Vec3(0,1f,0);
    private static float fovO = 0;
    private static boolean isLockPos = false;
    private static CameraAnimation.Pose pose_;

    /**
     * 1.20.1EntityViewRenderEvent.CameraSetup改名成ViewportEvent.ComputeCameraAngles
     * 找了好久....这哪里联想得到...还得翻源码
     */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void transformCam(ViewportEvent.ComputeCameraAngles event){
        CameraType cameraType = Minecraft.getInstance().options.getCameraType();
        if(cameraType.isFirstPerson() || cameraType.isMirrored()){
            return;
        }

        if (!(!isEnd || linking) || currentAnim == null) {
            return;
        }

        //System.out.println(isEnd + "   " + linking + " " + linkTick);

        if (orginal == null){
            isEnd = true;
            linking = false;
            return;
        }
        else {
            if(orginal.isRemoved()){
                isEnd = true;
                linking = false;
                return;
            }
        }

        Camera camera = event.getCamera();
        double partialTicks = event.getPartialTick();

        CameraAnimation.Pose pose;
        if(linking){
            pose = pose_;
            float t = (linkTick + (float) partialTicks)/maxLinkTick;
            Vec3 Coord = orginal.getPosition((float) partialTicks);
            Vec3 targetPos = camera.getPosition();
            Vec3 lastFramePos = (pose.pos.yRot((float) Math.toRadians(-yawLock-90f)))
                    .add(isLockPos ? posLock : Coord);

            Vec3 targetRelate = targetPos.subtract(Coord);
            Vec3 lastRelate = lastFramePos.subtract(Coord);

            targetRelate = ToCylindricalCoordinate(targetRelate);
            lastRelate = ToCylindricalCoordinate(lastRelate);

            Vec3 camPos = LerpMinCylindrical(lastRelate,targetRelate,t);
            camPos = ToCartesianCoordinates(camPos).add(Coord);


            //camPos = CamAnim.Pose.lerpVec3(lastFramePos, targetPos, t);

            float tmp = event.getYaw() - (yawLock-pose.rotY);
            tmp = tmp%360f;

            if(tmp > 0){
                tmp -= 360;
                tmp = Math.abs(tmp) > tmp+360f ? tmp+360 : tmp;
            }

            float _rot_y =  yawLock-pose.rotY + tmp*t;
            float _rot_x = lerpBetween(pose.rotX, event.getPitch(), t);

            //p1nero: 1.20.1这俩变私有了，只能暴力AT...
            camera.setRotation(_rot_y, _rot_x);
            camera.setPosition(camPos);
            Minecraft.getInstance().options.fov().set(((int) lerpBetween(pose.fov, fovO, t)));

            //camera.
            //event.move
            event.setYaw(_rot_y);
            event.setPitch(_rot_x);
        }
        else {
            pose = currentAnim.getPose((tick + (float) partialTicks) / 20f);
            pose_ = pose;

            //Vec3 curPos = camera.getPosition();

            Vec3 camPos = (pose.pos.yRot((float) Math.toRadians(-yawLock-90f))).add(isLockPos ? posLock : orginal.getPosition((float) partialTicks));

            //Vec3 p = camPos.subtract(curPos);

            //System.out.println(pose);

            //cameraAccessor.invokeMove(p.x,p.y,p.z);
            camera.setRotation(yawLock-pose.rotY, pose.rotX);
            camera.setPosition(camPos);
            Minecraft.getInstance().options.fov().set((int)pose.fov);
            event.setYaw(yawLock-pose.rotY);
            event.setPitch(pose.rotX);
            //event.move
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event){
        if(Minecraft.getInstance().isPaused()) return;

        if(event.phase == TickEvent.Phase.START){
            if (!isEnd) tick++;
            if (linking) linkTick++;
        }
        else return;

        if(!isEnd && currentAnim != null && tick/20f >= currentAnim.totalTime){
            isEnd = true;
            linking = true;
            tick = 0;
            linkTick = 0;
        }

        if(linking && currentAnim != null && linkTick >= maxLinkTick) {
            isEnd = true;
            linking = false;
            linkTick = 0;
            tick = 0;
            Minecraft.getInstance().options.fov().set(((int) fovO));
        }
    }
    public static void SetAnim(CameraAnimation anim, LivingEntity org, boolean lockOrgPos){
        if(org instanceof Player){
            if (!((Player) org).isLocalPlayer()) return;
        }
        else {
            return;
        }

        if (!isEnd || linking)
            Minecraft.getInstance().options.fov().set(((int) fovO));

        orginal = org;
        yawLock = org.getViewYRot(0);
        posLock = org.position();
        linking = false;
        isEnd = false;
        tick = 0;
        linkTick = 0;
        maxLinkTick = 8;
        currentAnim = anim;
        isLockPos = lockOrgPos;
        fovO = Minecraft.getInstance().options.fov().get();
    }

    public static Vec3 ToCylindricalCoordinate(Vec3 in){
        double at = Math.atan2(-in.z ,in.x);
        double r = Math.sqrt(in.x*in.x + in.z*in.z);
        return new Vec3(r,at,in.y);  // r a h
    }

    public static final Double PI2 = Math.PI*2;
    public static Vec3 LerpMinCylindrical(Vec3 v1, Vec3 v2, float t){
        double v = (v2.y - v1.y)%PI2;

        if(v > 0){
            v -= PI2;
            v = Math.abs(v) > v + PI2 ? v+PI2 : v;
        }

        return new Vec3(v1.x*(1-t) + v2.x*t,
                v1.y + v*t,
                v1.z*(1-t) + v2.z*t);
    }

    public static float lerpBetween(float f1, float f2, float zero2one) {
        float f = 0.0F;

        for(f = f2 - f1; f < -180.0F; f += 360.0F) {
        }

        while(f >= 180.0F) {
            f -= 360.0F;
        }

        return f1 + zero2one * f;
    }


    public static Vec3 ToCartesianCoordinates(Vec3 in){
        return new Vec3(
                in.x*Math.cos(in.y),
                in.z,
                in.x*Math.sin(-in.y));
    }

}
