package com.falsaalucinacion.item;

import com.falsaalucinacion.LaFalsaAlucinacion;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class FalsaItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, LaFalsaAlucinacion.MODID);
    public static final RegistryObject<Item> GEMA_LIBERTAD = ITEMS.register("gema_libertad",
            () -> new Item(new Item.Properties().stacksTo(1).fireResistant()));
    public static final RegistryObject<Item> PICO_GLITCH = ITEMS.register("pico_glitch",
            () -> new Item(new Item.Properties().stacksTo(1).durability(240).fireResistant()));
    public static final RegistryObject<Item> RUIGH_GLITCHEADO = ITEMS.register("ruigh_glitcheado",
            () -> new GlitchedRelicItem(new Item.Properties().stacksTo(1).fireResistant()));

    private FalsaItems() {}
}
