package net.p1nero.ss.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.p1nero.ss.SwordSoaring;

public class ModItemTabs {

    public static final DeferredRegister<CreativeModeTab> ITEM_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SwordSoaring.MOD_ID);

    public static final RegistryObject<CreativeModeTab> WEAPON = ITEM_TABS.register("weapon",
            () -> CreativeModeTab.builder()
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .withTabsAfter(new ResourceLocation(SwordSoaring.MOD_ID, "spawn_egg"))
                    .title(Component.translatable("item_group.sword_soaring.weapon"))
                    .displayItems(new CreativeModeTab.DisplayItemsGenerator() {
                        @Override
                        public void accept(CreativeModeTab.ItemDisplayParameters itemDisplayParameters, CreativeModeTab.Output output) {

                        }
                    })
                    .icon(() -> new ItemStack(ModItems.LOONG_ROAR.get()))
                    .displayItems((parameters, tabData) -> {

                        tabData.accept(ModItems.LOONG_ROAR.get());

                    }).build());

}
