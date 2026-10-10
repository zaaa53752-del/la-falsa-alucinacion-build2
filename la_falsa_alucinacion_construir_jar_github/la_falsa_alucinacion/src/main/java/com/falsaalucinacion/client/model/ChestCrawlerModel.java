package com.falsaalucinacion.client.model;

import com.falsaalucinacion.entity.ChestCrawlerEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public final class ChestCrawlerModel extends HierarchicalModel<ChestCrawlerEntity> {
    private final ModelPart root;
    private final ModelPart lid;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;

    public ChestCrawlerModel(ModelPart root) {
        this.root = root;
        this.lid = root.getChild("lid");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
        this.leg4 = root.getChild("leg4");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-7.0F, -5.0F, -5.0F, 14.0F, 10.0F, 10.0F), PartPose.offset(0, 0, 0));
        root.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 22)
                .addBox(-7.0F, -2.0F, -5.0F, 14.0F, 3.0F, 10.0F), PartPose.offset(0, -5.0F, 0));
        root.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(0, 40).addBox(-1.0F, 0, -1.0F, 2, 5, 2), PartPose.offset(5, 3, 4));
        root.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(8, 40).addBox(-1.0F, 0, -1.0F, 2, 5, 2), PartPose.offset(-5, 3, 4));
        root.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(16, 40).addBox(-1.0F, 0, -1.0F, 2, 5, 2), PartPose.offset(5, 3, -4));
        root.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(24, 40).addBox(-1.0F, 0, -1.0F, 2, 5, 2), PartPose.offset(-5, 3, -4));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(ChestCrawlerEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        lid.xRot = -0.07F + Mth.sin(ageInTicks * 0.12F) * 0.05F;
        leg1.xRot = Mth.cos(limbSwing * 0.8F) * 0.6F * limbSwingAmount;
        leg4.xRot = Mth.cos(limbSwing * 0.8F) * 0.6F * limbSwingAmount;
        leg2.xRot = Mth.cos(limbSwing * 0.8F + (float)Math.PI) * 0.6F * limbSwingAmount;
        leg3.xRot = Mth.cos(limbSwing * 0.8F + (float)Math.PI) * 0.6F * limbSwingAmount;
        root.yRot = Mth.sin(ageInTicks * 0.1F) * 0.04F;
        root.y = Mth.sin(ageInTicks * 0.2F) * 0.08F;
    }

    @Override public ModelPart root() { return root; }
}
