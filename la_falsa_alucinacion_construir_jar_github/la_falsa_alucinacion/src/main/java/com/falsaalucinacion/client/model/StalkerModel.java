package com.falsaalucinacion.client.model;

import com.falsaalucinacion.entity.StalkerEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public final class StalkerModel extends HierarchicalModel<StalkerEntity> {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public StalkerModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 16)
                .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.offset(0.0F, -6.0F, 0.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-3.0F, 0.0F, -2.0F, 6.0F, 14.0F, 4.0F), PartPose.offset(0.0F, -6.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 0)
                .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 17.0F, 3.0F), PartPose.offset(4.3F, -4.0F, 0.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(32, 22)
                .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 17.0F, 3.0F), PartPose.offset(-4.3F, -4.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(48, 0)
                .addBox(-1.4F, 0.0F, -1.5F, 3.0F, 17.0F, 3.0F), PartPose.offset(1.8F, 7.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(48, 22)
                .addBox(-1.4F, 0.0F, -1.5F, 3.0F, 17.0F, 3.0F), PartPose.offset(-1.8F, 7.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(StalkerEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * ((float)Math.PI / 180F) + Mth.sin(ageInTicks * 0.13F) * 0.12F;
        head.xRot = headPitch * ((float)Math.PI / 180F) + Mth.sin(ageInTicks * 0.075F) * 0.09F;
        body.yRot = Mth.sin(ageInTicks * 0.085F) * 0.18F;
        body.xRot = Mth.sin(ageInTicks * 0.12F) * 0.035F;
        leftArm.xRot = Mth.cos(limbSwing * 0.62F) * 0.75F * limbSwingAmount - 0.16F;
        rightArm.xRot = Mth.cos(limbSwing * 0.62F + (float)Math.PI) * 0.75F * limbSwingAmount - 0.16F;
        leftArm.zRot = 0.10F + Mth.sin(ageInTicks * 0.19F) * 0.07F;
        rightArm.zRot = -0.10F - Mth.sin(ageInTicks * 0.19F + 1.3F) * 0.07F;
        leftLeg.xRot = Mth.cos(limbSwing * 0.62F) * 0.72F * limbSwingAmount;
        rightLeg.xRot = Mth.cos(limbSwing * 0.62F + (float)Math.PI) * 0.72F * limbSwingAmount;
    }

    @Override public ModelPart root() { return root; }
}
