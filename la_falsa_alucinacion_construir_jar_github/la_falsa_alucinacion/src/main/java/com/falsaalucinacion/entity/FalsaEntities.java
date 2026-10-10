package com.falsaalucinacion.entity;

import com.falsaalucinacion.LaFalsaAlucinacion;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class FalsaEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, LaFalsaAlucinacion.MODID);

    public static final RegistryObject<EntityType<StalkerEntity>> ACECHADOR = ENTITY_TYPES.register("acechador",
            () -> EntityType.Builder.of(StalkerEntity::new, MobCategory.MONSTER)
                    .sized(0.62F, 2.75F).clientTrackingRange(64).updateInterval(1).build("acechador"));

    public static final RegistryObject<EntityType<ChestCrawlerEntity>> COFRE_ERRANTE = ENTITY_TYPES.register("cofre_errante",
            () -> EntityType.Builder.of(ChestCrawlerEntity::new, MobCategory.MONSTER)
                    .sized(1.1F, 1.15F).clientTrackingRange(64).updateInterval(2).build("cofre_errante"));

    private FalsaEntities() {}
}
