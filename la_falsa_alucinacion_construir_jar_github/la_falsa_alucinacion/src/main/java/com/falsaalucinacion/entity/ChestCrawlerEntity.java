package com.falsaalucinacion.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public final class ChestCrawlerEntity extends Monster {
    public ChestCrawlerEntity(EntityType<? extends Monster> type, Level level) { super(type, level); }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 18.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.16D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.25D));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.tickCount % 20 == 0) {
            Player player = this.level().getNearestPlayer(this, 30.0D);
            if (player != null && player.level().dimension().location().getNamespace().equals("falsaalucinacion")) {
                this.getNavigation().moveTo(player, 0.32D);
            }
        }
        if (!this.level().isClientSide && this.tickCount % 8 == 0 && this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.WITCH, this.getX(), this.getY() + 0.55D, this.getZ(),
                    4, 0.4D, 0.2D, 0.4D, 0.015D);
        }
    }

    @Override protected SoundEvent getAmbientSound() { return SoundEvents.CHEST_OPEN; }
    @Override protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) { return SoundEvents.CHEST_CLOSE; }
}
