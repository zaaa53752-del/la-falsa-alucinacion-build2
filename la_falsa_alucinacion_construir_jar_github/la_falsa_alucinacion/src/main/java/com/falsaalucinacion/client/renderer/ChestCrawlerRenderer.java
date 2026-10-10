package com.falsaalucinacion.client.renderer;

import com.falsaalucinacion.LaFalsaAlucinacion;
import com.falsaalucinacion.client.FalsaClientModEvents;
import com.falsaalucinacion.client.model.ChestCrawlerModel;
import com.falsaalucinacion.entity.ChestCrawlerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class ChestCrawlerRenderer extends MobRenderer<ChestCrawlerEntity, ChestCrawlerModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(LaFalsaAlucinacion.MODID, "textures/entity/cofre_errante.png");

    public ChestCrawlerRenderer(EntityRendererProvider.Context context) {
        super(context, new ChestCrawlerModel(context.bakeLayer(FalsaClientModEvents.CHEST_LAYER)), 0.38F);
    }

    @Override public ResourceLocation getTextureLocation(ChestCrawlerEntity entity) { return TEXTURE; }
}
