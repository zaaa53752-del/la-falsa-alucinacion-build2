package com.falsaalucinacion.item;

import com.falsaalucinacion.LaFalsaAlucinacion;
import com.falsaalucinacion.FalsaGameplayEvents;
import com.falsaalucinacion.client.FalsaClientEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public final class GlitchedRelicItem extends Item {
    public GlitchedRelicItem(Properties properties) { super(properties); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.level().dimension().equals(LaFalsaAlucinacion.FALSE_REALITY)) {
            if (!level.isClientSide && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                FalsaGameplayEvents.startEnding(serverPlayer);
            } else if (level.isClientSide) {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FalsaClientEvents.beginEnding());
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
