package com.falsaalucinacion;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class FalsaBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, LaFalsaAlucinacion.MODID);
    public static final RegistryObject<Block> BARRERA_METALICA = BLOCKS.register("barrera_metalica",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(-1.0F, 3_600_000.0F)
                    .noLootTable().sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops()));
    public static final RegistryObject<BlockItem> BARRERA_METALICA_ITEM = LaFalsaAlucinacionItems.registerBarrierItem();

    private FalsaBlocks() {}
}

final class LaFalsaAlucinacionItems {
    private LaFalsaAlucinacionItems() {}
    static RegistryObject<BlockItem> registerBarrierItem() {
        return com.falsaalucinacion.item.FalsaItems.ITEMS.register("barrera_metalica",
                () -> new BlockItem(FalsaBlocks.BARRERA_METALICA.get(), new Item.Properties()));
    }
}
