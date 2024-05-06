package net.p1nero.ss;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SwordSoaring.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.DoubleValue FLY_SPEED_SCALE = createDouble("the ratio of flying speed to view vector","fly_speed_scale", 0.6);
    public static final ForgeConfigSpec.DoubleValue STAMINA_CONSUME_PER_TICK = createDouble("the stamina consumed per tick when flying" ,"stamina_consume_per_tick", 0.05);
    public static final ForgeConfigSpec.DoubleValue MAX_ANTICIPATION_TICK = createDouble("ticks of pre taking off","max_anticipation_tick", 10);
    static final ForgeConfigSpec SPEC = BUILDER.build();

    private static ForgeConfigSpec.BooleanValue createBool(String key, boolean defaultValue){
        return BUILDER
                .comment(I18n.get("config."+SwordSoaring.MOD_ID+"."+key))
                .translation("config."+SwordSoaring.MOD_ID+"."+key)
                .define(key, defaultValue);
    }

    private static ForgeConfigSpec.DoubleValue createDouble(String key, double defaultValue) {
        return createDouble(I18n.get("config."+SwordSoaring.MOD_ID+"."+key), key, defaultValue);
    }

    private static ForgeConfigSpec.DoubleValue createDouble(String comment ,String key, double defaultValue) {
        return BUILDER
                .comment(comment)
                .translation("config."+SwordSoaring.MOD_ID+"."+key)
                .defineInRange(key, defaultValue, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("sword_soaring").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                .then(Commands.literal("fly_speed_scale")
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                                .executes((context) -> setConfig(FLY_SPEED_SCALE, DoubleArgumentType.getDouble(context, "value"), context.getSource()))
                        )
                )
                .then(Commands.literal("stamina_consume_per_tick")
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                                .executes((context) -> setConfig(STAMINA_CONSUME_PER_TICK, DoubleArgumentType.getDouble(context, "value"), context.getSource()))
                        )
                )
                .then(Commands.literal("max_anticipation_tick")
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                                .executes((context) -> setConfig(MAX_ANTICIPATION_TICK, DoubleArgumentType.getDouble(context, "value"), context.getSource()))
                        )
                )
        );
    }

    private static int setConfig(ForgeConfigSpec.DoubleValue config, double value, CommandSourceStack stack){
        config.set(value);
        if(stack.isPlayer()){
            stack.getPlayer().sendSystemMessage(Component.literal("Successfully set to : "+value));
            if(value<=0){
                stack.getPlayer().sendSystemMessage(Component.literal("Waring! It's a strange value :D"));
            }
        }
        return 0;
    }

}
