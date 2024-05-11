package net.p1nero.ss;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = SwordSoaring.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.BooleanValue HIDE_SWORD_WHEN_FLY = createBool("hide the sword in your hand when flying","hide_sword_when_fly", true);
    public static final ForgeConfigSpec.BooleanValue ENABLE_INERTIA = createBool("enable_inertia", true);
    public static final ForgeConfigSpec.DoubleValue INERTIA_TICK_BEFORE = createDouble("the inertia tick.(delay time) only work when enable_inertia is true. Shouldn't larger than 100!!!","inertia_tick_before", 10);
    public static final ForgeConfigSpec.DoubleValue FLY_SPEED_SCALE = createDouble("the ratio of flying speed to view vector","fly_speed_scale", 0.6);
    public static final ForgeConfigSpec.DoubleValue STAMINA_CONSUME_PER_TICK = createDouble("the stamina consumed per tick when flying" ,"stamina_consume_per_tick", 0.05);
    public static final ForgeConfigSpec.DoubleValue MAX_ANTICIPATION_TICK = createDouble("ticks of pre taking off","max_anticipation_tick", 10);
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of items considered as sword.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), Config::validateItemName);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static Set<Item> swordItems = new HashSet<>();

    private static ForgeConfigSpec.BooleanValue createBool(String key, boolean defaultValue){
        return BUILDER
                .translation("config."+SwordSoaring.MOD_ID+"."+key)
                .define(key, defaultValue);
    }
    private static ForgeConfigSpec.BooleanValue createBool(String comment, String key, boolean defaultValue){
        return BUILDER
                .comment(comment)
                .translation("config."+SwordSoaring.MOD_ID+"."+key)
                .define(key, defaultValue);
    }

    private static ForgeConfigSpec.DoubleValue createDouble(String comment ,String key, double defaultValue) {
        return BUILDER
                .comment(comment)
                .translation("config."+SwordSoaring.MOD_ID+"."+key)
                .defineInRange(key, defaultValue, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    private static boolean validateItemName(final Object obj){
        return obj instanceof final String itemName && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemName));
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("sword_soaring").requires((commandSourceStack) -> commandSourceStack.hasPermission(2))
                .then(Commands.literal("hide_sword_when_fly")
                        .then(Commands.argument("value", BoolArgumentType.bool())
                                .executes((context) -> setConfig(HIDE_SWORD_WHEN_FLY, BoolArgumentType.getBool(context, "value"), context.getSource()))
                        )
                )
                .then(Commands.literal("enable_inertia")
                        .then(Commands.argument("value", BoolArgumentType.bool())
                                .executes((context) -> setConfig(ENABLE_INERTIA, BoolArgumentType.getBool(context, "value"), context.getSource()))
                        )
                )
                .then(Commands.literal("inertia_tick_before")
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                                .executes((context) -> setConfig(INERTIA_TICK_BEFORE, DoubleArgumentType.getDouble(context, "value"), context.getSource()))
                        )
                )
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

    private static int setConfig(ForgeConfigSpec.BooleanValue config, boolean value, CommandSourceStack stack){
        config.set(value);
        if(stack.isPlayer()){
            stack.getPlayer().sendSystemMessage(Component.literal("Successfully set to : "+value));
        }
        return 0;
    }

}
