package com.falsaalucinacion.client;

import com.falsaalucinacion.LaFalsaAlucinacion;
import com.falsaalucinacion.client.model.ChestCrawlerModel;
import com.falsaalucinacion.client.model.StalkerModel;
import com.falsaalucinacion.client.renderer.ChestCrawlerRenderer;
import com.falsaalucinacion.client.renderer.StalkerRenderer;
import com.falsaalucinacion.entity.FalsaEntities;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LaFalsaAlucinacion.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FalsaClientModEvents {
    public static final ModelLayerLocation STALKER_LAYER = new ModelLayerLocation(new ResourceLocation(LaFalsaAlucinacion.MODID, "acechador"), "main");
    public static final ModelLayerLocation CHEST_LAYER = new ModelLayerLocation(new ResourceLocation(LaFalsaAlucinacion.MODID, "cofre_errante"), "main");

    private FalsaClientModEvents() {}

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(STALKER_LAYER, StalkerModel::createBodyLayer);
        event.registerLayerDefinition(CHEST_LAYER, ChestCrawlerModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(FalsaEntities.ACECHADOR.get(), StalkerRenderer::new);
        event.registerEntityRenderer(FalsaEntities.COFRE_ERRANTE.get(), ChestCrawlerRenderer::new);
    }
}
