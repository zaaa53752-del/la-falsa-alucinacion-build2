package com.falsaalucinacion;

import com.falsaalucinacion.client.FalsaClientEvents;
import com.falsaalucinacion.entity.FalsaEntities;
import com.falsaalucinacion.item.FalsaItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(LaFalsaAlucinacion.MODID)
public final class LaFalsaAlucinacion {
    public static final String MODID = "falsaalucinacion";
    public static final ResourceKey<Level> FALSE_REALITY = ResourceKey.create(
            Registries.DIMENSION, new ResourceLocation(MODID, "falsa_realidad"));

    public LaFalsaAlucinacion() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        FalsaBlocks.BLOCKS.register(modBus);
        FalsaItems.ITEMS.register(modBus);
        FalsaEntities.ENTITY_TYPES.register(modBus);
        modBus.addListener(this::addEntityAttributes);
        modBus.addListener(this::addCreativeItems);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FalsaClientEvents.bootstrap());
    }

    private void addEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(FalsaEntities.ACECHADOR.get(), com.falsaalucinacion.entity.StalkerEntity.createAttributes().build());
        event.put(FalsaEntities.COFRE_ERRANTE.get(), com.falsaalucinacion.entity.ChestCrawlerEntity.createAttributes().build());
    }

    private void addCreativeItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == net.minecraft.world.item.CreativeModeTabs.INGREDIENTS) {
            event.accept(FalsaItems.GEMA_LIBERTAD);
            event.accept(FalsaItems.RUIGH_GLITCHEADO);
        }
        if (event.getTabKey() == net.minecraft.world.item.CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(FalsaItems.PICO_GLITCH);
        }
        if (event.getTabKey() == net.minecraft.world.item.CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(FalsaBlocks.BARRERA_METALICA_ITEM);
        }
    }
}
