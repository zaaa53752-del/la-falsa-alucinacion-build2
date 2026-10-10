package com.falsaalucinacion.client.renderer;

import com.falsaalucinacion.LaFalsaAlucinacion;
import com.falsaalucinacion.client.FalsaClientModEvents;
import com.falsaalucinacion.client.model.StalkerModel;
import com.falsaalucinacion.entity.StalkerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class StalkerRenderer extends MobRenderer<StalkerEntity, StalkerModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(LaFalsaAlucinacion.MODID, "textures/entity/acechador.png");

    public StalkerRenderer(EntityRendererProvider.Context context) {
        super(context, new StalkerModel(context.bakeLayer(FalsaClientModEvents.STALKER_LAYER)), 0.52F);
    }

    @Override public ResourceLocation getTextureLocation(StalkerEntity entity) { return TEXTURE; }
}
