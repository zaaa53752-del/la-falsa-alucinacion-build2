package com.falsaalucinacion;

import com.falsaalucinacion.entity.ChestCrawlerEntity;
import com.falsaalucinacion.entity.FalsaEntities;
import com.falsaalucinacion.entity.StalkerEntity;
import com.falsaalucinacion.item.FalsaItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = LaFalsaAlucinacion.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FalsaGameplayEvents {
    private static final String ACTIVE = "FA_ACTIVE";
    private static final int EPISODE_DAY = 4; // dayTime starts at zero: index 4 is the fifth Minecraft day.

    public FalsaGameplayEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        CompoundTag data = player.getPersistentData();

        if (!data.getBoolean(ACTIVE)) {
            if (!data.getBoolean("FA_PLAYED") && player.level().dimension() == Level.OVERWORLD) {
                long time = player.serverLevel().getDayTime();
                long day = time / 24000L;
                long inDay = Math.floorMod(time, 24000L);
                if (day >= EPISODE_DAY && inDay >= 13000L && inDay < 23000L) beginEpisode(player);
            }
            return;
        }

        if (!player.level().dimension().equals(LaFalsaAlucinacion.FALSE_REALITY)) return;
        ServerLevel level = player.serverLevel();
        int elapsed = data.getInt("FA_ELAPSED") + 1;
        data.putInt("FA_ELAPSED", elapsed);

        // Forced survival: the world can hurt, but it cannot end the episode early.
        if (player.getHealth() < player.getMaxHealth()) player.setHealth(player.getMaxHealth());
        if ((elapsed % 10) == 0) {
            player.getFoodData().setFoodLevel(20);
            player.getFoodData().setSaturation(20.0F);
        }

        // A descent changes the world's clock to night; climbing back up forces dawn.
        double lastY = data.contains("FA_LAST_Y") ? data.getDouble("FA_LAST_Y") : player.getY();
        double nowY = player.getY();
        double baseline = data.contains("FA_HEIGHT_BASE") ? data.getDouble("FA_HEIGHT_BASE") : nowY;
        if (baseline - nowY > 3.0D) {
            level.setDayTime(18000L);
            data.putDouble("FA_HEIGHT_BASE", nowY);
        } else if (nowY - baseline > 3.0D) {
            level.setDayTime(6000L);
            data.putDouble("FA_HEIGHT_BASE", nowY);
        }
        data.putDouble("FA_LAST_Y", nowY);

        if (player.getInventory().contains(new ItemStack(FalsaItems.GEMA_LIBERTAD.get()))) {
            gemAuraAndGuidance(level, player, data, elapsed);
        }

        if (elapsed > 250 && elapsed % 520 == 0 && !player.getInventory().contains(new ItemStack(FalsaItems.GEMA_LIBERTAD.get()))) {
            spawnLootChest(level, player, data);
        }

        int phase = Math.min(3, elapsed / 6000);
        if (elapsed > 450 && elapsed % 640 == 0) spawnStalkerIfNeeded(level, player);
        if (elapsed > 900 && elapsed % 1450 == 0) spawnChestCrawlerIfNeeded(level, player);
        if (elapsed > 750 && elapsed % 390 == 0) spawnMutant(level, player, phase);

        // The green phase strips nearby trees and brings down meteorites.
        if (phase >= 3 && elapsed % 25 == 0) eraseTouchedTrees(level, player);
        if (phase >= 3 && elapsed % 10 == 0) animateFlyingCabins(level, data, elapsed);
        if (phase >= 3) handleMeteor(level, player, data, elapsed);

        int ending = data.getInt("FA_ENDING_TICKS");
        if (ending > 0) {
            ending++;
            data.putInt("FA_ENDING_TICKS", ending);
            if (ending >= 235) finishEpisode(player, data);
        }
    }

    private static void beginEpisode(ServerPlayer player) {
        ServerLevel originalLevel = player.serverLevel();
        ServerLevel falseLevel = player.getServer().getLevel(LaFalsaAlucinacion.FALSE_REALITY);
        if (falseLevel == null) return;

        CompoundTag data = player.getPersistentData();
        data.putString("FA_ORIGINAL_DIM", originalLevel.dimension().location().toString());
        data.putDouble("FA_ORIGINAL_X", player.getX());
        data.putDouble("FA_ORIGINAL_Y", player.getY());
        data.putDouble("FA_ORIGINAL_Z", player.getZ());
        data.putFloat("FA_ORIGINAL_YAW", player.getYRot());
        data.putFloat("FA_ORIGINAL_PITCH", player.getXRot());
        data.putLong("FA_ORIGINAL_TIME", originalLevel.getDayTime());
        data.putFloat("FA_ORIGINAL_HEALTH", player.getHealth());
        data.putInt("FA_ORIGINAL_FOOD", player.getFoodData().getFoodLevel());
        data.putFloat("FA_ORIGINAL_SAT", player.getFoodData().getSaturationLevel());
        data.put("FA_ORIGINAL_INV", player.getInventory().save(new ListTag()).copy());
        data.putBoolean(ACTIVE, true);
        data.putBoolean("FA_PLAYED", true);
        data.putInt("FA_ELAPSED", 0);
        data.putInt("FA_CHEST_COUNT", 0);
        data.putInt("FA_METEOR_TIMER", 0);
        data.putInt("FA_METEOR_ACTIVE", 0);

        int sx = 0;
        int sz = 0;
        falseLevel.setDayTime(0L);
        falseLevel.getChunkAt(new BlockPos(sx, 0, sz));
        int surfaceY = falseLevel.getHeight(Heightmap.Types.WORLD_SURFACE, sx, sz);
        makeCorruptedForest(falseLevel, sx, sz, surfaceY);
        int barrierZ = sz + 62;
        buildMetalBarrier(falseLevel, sx, barrierZ, surfaceY);
        data.putInt("FA_SURFACE_Y", surfaceY);
        data.putInt("FA_CABIN1_X", 42);
        data.putInt("FA_CABIN1_Z", -18);
        data.putInt("FA_CABIN1_BASE", surfaceY + 9);
        data.putInt("FA_CABIN1_Y", surfaceY + 9);
        data.putInt("FA_CABIN2_X", -42);
        data.putInt("FA_CABIN2_Z", 27);
        data.putInt("FA_CABIN2_BASE", surfaceY + 13);
        data.putInt("FA_CABIN2_Y", surfaceY + 13);
        buildGlitchedCabin(falseLevel, 42, surfaceY + 9, -18);
        buildGlitchedCabin(falseLevel, -42, surfaceY + 13, 27);
        data.putInt("FA_TARGET_X", sx);
        data.putInt("FA_TARGET_Y", surfaceY + 2);
        data.putInt("FA_TARGET_Z", barrierZ);

        player.getInventory().clearContent();
        player.getInventory().setChanged();
        player.teleportTo(falseLevel, sx + 0.5D, surfaceY, sz + 0.5D, Set.of(), player.getYRot(), player.getXRot());
        player.setHealth(player.getMaxHealth());
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(20.0F);
        falseLevel.playSound(null, sx, surfaceY, sz, SoundEvents.AMBIENT_CAVE, SoundSource.AMBIENT, 0.9F, 0.55F);
    }

    public static void startEnding(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.getBoolean(ACTIVE) || !player.level().dimension().equals(LaFalsaAlucinacion.FALSE_REALITY)) return;
        data.putInt("FA_ENDING_TICKS", 1);
        ServerLevel level = player.serverLevel();
        // The figures that were only watching now close in during the false return to normality.
        for (int i = 0; i < 5; i++) spawnMutant(level, player, 3);
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_SCREAM, SoundSource.HOSTILE, 1.0F, 0.62F);
    }

    private static void finishEpisode(ServerPlayer player, CompoundTag data) {
        String dimName = data.getString("FA_ORIGINAL_DIM");
        ResourceLocation location = ResourceLocation.tryParse(dimName);
        if (location == null) return;
        ResourceKey<Level> key = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, location);
        ServerLevel destination = player.getServer().getLevel(key);
        if (destination == null) destination = player.getServer().overworld();

        double x = data.getDouble("FA_ORIGINAL_X");
        double y = data.getDouble("FA_ORIGINAL_Y");
        double z = data.getDouble("FA_ORIGINAL_Z");
        float yaw = data.getFloat("FA_ORIGINAL_YAW");
        float pitch = data.getFloat("FA_ORIGINAL_PITCH");
        player.teleportTo(destination, x, y, z, Set.of(), yaw, pitch);
        destination.setDayTime(data.getLong("FA_ORIGINAL_TIME"));
        player.getInventory().clearContent();
        player.getInventory().load(data.getList("FA_ORIGINAL_INV", Tag.TAG_COMPOUND));
        player.getInventory().setChanged();
        player.setHealth(Mth.clamp(data.getFloat("FA_ORIGINAL_HEALTH"), 1.0F, player.getMaxHealth()));
        player.getFoodData().setFoodLevel(data.getInt("FA_ORIGINAL_FOOD"));
        player.getFoodData().setSaturation(data.getFloat("FA_ORIGINAL_SAT"));
        data.putBoolean(ACTIVE, false);
        data.putInt("FA_ENDING_TICKS", 0);
        data.putInt("FA_ELAPSED", 0);
        destination.playSound(null, x, y, z, SoundEvents.AMBIENT_CAVE, SoundSource.AMBIENT, 0.18F, 1.6F);
    }

    private static void makeCorruptedForest(ServerLevel level, int cx, int cz, int surfaceY) {
        RandomSource random = level.random;
        for (int i = 0; i < 25; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            int radius = 5 + random.nextInt(29);
            int x = cx + (int) (Math.cos(angle) * radius);
            int z = cz + (int) (Math.sin(angle) * radius);
            int ground = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
            int height = 4 + random.nextInt(4);
            for (int j = 0; j < height; j++) level.setBlock(new BlockPos(x, ground + j, z), Blocks.OAK_LOG.defaultBlockState(), 3);
            for (int dy = -1; dy <= 2; dy++) {
                int width = dy == 2 ? 1 : 2;
                for (int dx = -width; dx <= width; dx++) {
                    for (int dz = -width; dz <= width; dz++) {
                        if (Math.abs(dx) == width && Math.abs(dz) == width && random.nextBoolean()) continue;
                        BlockPos leaf = new BlockPos(x + dx, ground + height - 2 + dy, z + dz);
                        if (level.isEmptyBlock(leaf)) level.setBlock(leaf, Blocks.OAK_LEAVES.defaultBlockState().setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true), 3);
                    }
                }
            }
        }
    }

    private static void buildMetalBarrier(ServerLevel level, int centerX, int z, int surfaceY) {
        for (int dx = -5; dx <= 5; dx++) {
            for (int dy = 0; dy <= 7; dy++) {
                // The whole wall is impossible to mine except for its central two-block seam.
                level.setBlock(new BlockPos(centerX + dx, surfaceY + dy, z), FalsaBlocks.BARRERA_METALICA.get().defaultBlockState(), 3);
            }
        }
        BlockPos chestPos = new BlockPos(centerX, surfaceY, z + 3);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), 3);
        BlockEntity blockEntity = level.getBlockEntity(chestPos);
        if (blockEntity instanceof ChestBlockEntity chest) {
            chest.setItem(13, new ItemStack(FalsaItems.RUIGH_GLITCHEADO.get()));
            chest.setChanged();
        }
    }

    private static void buildGlitchedCabin(ServerLevel level, int cx, int baseY, int cz) {
        // Deliberately impossible little huts: the final phase makes them float like startled birds.
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                level.setBlock(new BlockPos(cx + dx, baseY, cz + dz), Blocks.DARK_OAK_PLANKS.defaultBlockState(), 3);
                for (int dy = 1; dy <= 3; dy++) {
                    boolean edge = Math.abs(dx) == 2 || Math.abs(dz) == 2;
                    if (!edge) continue;
                    if (dy == 2 && (dx == 0 || dz == 0)) continue;
                    level.setBlock(new BlockPos(cx + dx, baseY + dy, cz + dz),
                            (dx == -2 || dx == 2) ? Blocks.OAK_LOG.defaultBlockState() : Blocks.BIRCH_PLANKS.defaultBlockState(), 3);
                }
            }
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                level.setBlock(new BlockPos(cx + dx, baseY + 4, cz + dz), Blocks.DARK_OAK_PLANKS.defaultBlockState(), 3);
            }
        }
        level.setBlock(new BlockPos(cx, baseY + 2, cz - 1), Blocks.REDSTONE_LAMP.defaultBlockState(), 3);
    }

    private static void eraseGlitchedCabin(ServerLevel level, int cx, int baseY, int cz) {
        for (BlockPos pos : BlockPos.betweenClosed(cx - 2, baseY, cz - 2, cx + 2, baseY + 4, cz + 2)) {
            var state = level.getBlockState(pos);
            if (state.is(Blocks.DARK_OAK_PLANKS) || state.is(Blocks.BIRCH_PLANKS)
                    || state.is(Blocks.OAK_LOG) || state.is(Blocks.REDSTONE_LAMP)) {
                level.setBlock(pos.immutable(), Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    private static int triangleOffset(int n) {
        int step = Math.floorMod(n, 22);
        return step <= 11 ? step : 22 - step;
    }

    private static void animateFlyingCabins(ServerLevel level, CompoundTag data, int elapsed) {
        int firstX = data.getInt("FA_CABIN1_X"), firstZ = data.getInt("FA_CABIN1_Z");
        int firstOldY = data.getInt("FA_CABIN1_Y"), firstBase = data.getInt("FA_CABIN1_BASE");
        int firstNewY = firstBase + triangleOffset(elapsed / 10);
        if (firstNewY != firstOldY) {
            eraseGlitchedCabin(level, firstX, firstOldY, firstZ);
            buildGlitchedCabin(level, firstX, firstNewY, firstZ);
            data.putInt("FA_CABIN1_Y", firstNewY);
        }
        int secondX = data.getInt("FA_CABIN2_X"), secondZ = data.getInt("FA_CABIN2_Z");
        int secondOldY = data.getInt("FA_CABIN2_Y"), secondBase = data.getInt("FA_CABIN2_BASE");
        int secondNewY = secondBase + triangleOffset(elapsed / 10 + 8);
        if (secondNewY != secondOldY) {
            eraseGlitchedCabin(level, secondX, secondOldY, secondZ);
            buildGlitchedCabin(level, secondX, secondNewY, secondZ);
            data.putInt("FA_CABIN2_Y", secondNewY);
        }
    }

    private static void spawnLootChest(ServerLevel level, ServerPlayer player, CompoundTag data) {
        RandomSource random = level.random;
        Vec3 p = player.position();
        for (int attempt = 0; attempt < 12; attempt++) {
            int x = Mth.floor(p.x) + random.nextInt(35) - 17;
            int z = Mth.floor(p.z) + random.nextInt(35) - 17;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos pos = new BlockPos(x, y, z);
            if (!level.isEmptyBlock(pos) || !level.getBlockState(pos.below()).isSolid()) continue;
            level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 3);
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ChestBlockEntity chest) {
                chest.setItem(random.nextInt(27), new ItemStack(Items.ROTTEN_FLESH, 1 + random.nextInt(2)));
                chest.setItem(random.nextInt(27), new ItemStack(Items.BEEF, 2 + random.nextInt(4)));
                chest.setItem(random.nextInt(27), new ItemStack(Items.COOKED_BEEF, random.nextInt(2)));
                chest.setItem(random.nextInt(27), new ItemStack(Items.STICK, 2 + random.nextInt(4)));
                if (random.nextInt(3) == 0) chest.setItem(random.nextInt(27), new ItemStack(Items.COBWEB));
                int count = data.getInt("FA_CHEST_COUNT") + 1;
                data.putInt("FA_CHEST_COUNT", count);
                if (count >= 4 || random.nextInt(7) == 0) {
                    chest.setItem(random.nextInt(27), new ItemStack(FalsaItems.GEMA_LIBERTAD.get()));
                }
                chest.setChanged();
            }
            level.playSound(null, pos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.45F, 0.6F + random.nextFloat() * 0.25F);
            return;
        }
    }

    private static void gemAuraAndGuidance(ServerLevel level, ServerPlayer player, CompoundTag data, int elapsed) {
        Vec3 p = player.position();
        if (elapsed % 7 == 0) {
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                    p.x, p.y + 0.8D, p.z, 5, 0.45D, 0.7D, 0.45D, 0.005D);
        }
        if (elapsed % 10 == 0) {
            int tx = data.getInt("FA_TARGET_X");
            int ty = data.getInt("FA_TARGET_Y");
            int tz = data.getInt("FA_TARGET_Z");
            Vec3 delta = new Vec3(tx + 0.5D - p.x, ty + 0.5D - (p.y + 0.7D), tz + 0.5D - p.z);
            double length = delta.length();
            if (length > 0.1D) {
                Vec3 direction = delta.normalize();
                for (int i = 2; i < Math.min(20, (int) length); i += 2) {
                    Vec3 point = p.add(0, 0.7D, 0).add(direction.scale(i));
                    level.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL, point.x, point.y, point.z, 1, 0, 0, 0, 0);
                }
            }
        }
    }

    private static void spawnStalkerIfNeeded(ServerLevel level, ServerPlayer player) {
        if (!level.getEntitiesOfClass(StalkerEntity.class, player.getBoundingBox().inflate(30.0D)).isEmpty()) return;
        Vec3 look = player.getViewVector(1.0F);
        int x = Mth.floor(player.getX() - look.x * (9.0D + level.random.nextInt(7)));
        int z = Mth.floor(player.getZ() - look.z * (9.0D + level.random.nextInt(7)));
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        StalkerEntity stalker = FalsaEntities.ACECHADOR.get().create(level);
        if (stalker == null) return;
        stalker.moveTo(x + 0.5D, y, z + 0.5D, player.getYRot() + 180.0F, 0.0F);
        stalker.setPersistenceRequired();
        level.addFreshEntity(stalker);
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_STARE, SoundSource.HOSTILE, 0.55F, 0.65F);
    }

    private static void spawnChestCrawlerIfNeeded(ServerLevel level, ServerPlayer player) {
        if (!level.getEntitiesOfClass(ChestCrawlerEntity.class, player.getBoundingBox().inflate(35.0D)).isEmpty()) return;
        int x = Mth.floor(player.getX()) + level.random.nextInt(25) - 12;
        int z = Mth.floor(player.getZ()) + level.random.nextInt(25) - 12;
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        ChestCrawlerEntity chest = FalsaEntities.COFRE_ERRANTE.get().create(level);
        if (chest == null) return;
        chest.moveTo(x + 0.5D, y, z + 0.5D, 0, 0);
        chest.setPersistenceRequired();
        level.addFreshEntity(chest);
    }

    private static void spawnMutant(ServerLevel level, ServerPlayer player, int phase) {
        List<Monster> nearby = level.getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(28.0D));
        if (nearby.size() > 7) return;
        int x = Mth.floor(player.getX()) + level.random.nextInt(35) - 17;
        int z = Mth.floor(player.getZ()) + level.random.nextInt(35) - 17;
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        Entity entity;
        int choice = level.random.nextInt(3);
        if (choice == 0) entity = EntityType.ZOMBIE.create(level);
        else if (choice == 1) entity = EntityType.SKELETON.create(level);
        else entity = EntityType.CREEPER.create(level);
        if (!(entity instanceof Monster monster)) return;
        monster.moveTo(x + 0.5D, y, z + 0.5D, level.random.nextFloat() * 360.0F, 0);
        monster.setPersistenceRequired();
        monster.setGlowingTag(true);
        if (monster instanceof Zombie zombie) {
            zombie.setBaby(false);
            if (zombie.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) != null)
                zombie.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).setBaseValue(8.0D);
            zombie.setHealth(zombie.getMaxHealth());
        }
        if (monster instanceof Skeleton skeleton && phase >= 1) skeleton.setGlowingTag(true);
        level.addFreshEntity(monster);
    }

    private static void eraseTouchedTrees(ServerLevel level, ServerPlayer player) {
        BlockPos center = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2, -1, -2), center.offset(2, 3, 2))) {
            if (level.getBlockState(pos).is(BlockTags.LOGS) || level.getBlockState(pos).is(BlockTags.LEAVES)) {
                level.setBlock(pos.immutable(), Blocks.AIR.defaultBlockState(), 3);
                if (level.random.nextInt(8) == 0) level.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF,
                        pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 2, 0.2D, 0.3D, 0.2D, 0.01D);
            }
        }
    }

    private static void handleMeteor(ServerLevel level, ServerPlayer player, CompoundTag data, int elapsed) {
        int timer = data.getInt("FA_METEOR_TIMER");
        if (timer == 0 && elapsed > 6000 && elapsed % 1150 == 0) {
            int x = Mth.floor(player.getX()) + level.random.nextInt(17) - 8;
            int z = Mth.floor(player.getZ()) + level.random.nextInt(17) - 8;
            data.putInt("FA_METEOR_X", x);
            data.putInt("FA_METEOR_Z", z);
            data.putInt("FA_METEOR_TIMER", 55);
            level.playSound(null, x, player.getY() + 10, z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 0.6F, 1.5F);
        } else if (timer > 0) {
            timer--;
            data.putInt("FA_METEOR_TIMER", timer);
            int x = data.getInt("FA_METEOR_X");
            int z = data.getInt("FA_METEOR_Z");
            int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME, x + 0.5D, ground + timer / 4.0D + 1, z + 0.5D,
                    5, 0.6D, 0.6D, 0.6D, 0.035D);
            if (timer == 0) {
                level.explode(null, x + 0.5D, ground, z + 0.5D, 5.5F, Level.ExplosionInteraction.BLOCK);
                level.playSound(null, x, ground, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 2.4F, 0.55F);
            }
        }
    }

    @SubscribeEvent
    public static void onLookAtStalker(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)
                || !player.level().dimension().equals(LaFalsaAlucinacion.FALSE_REALITY) || player.tickCount % 3 != 0) return;
        Vec3 look = player.getViewVector(1.0F).normalize();
        for (StalkerEntity stalker : player.serverLevel().getEntitiesOfClass(StalkerEntity.class, player.getBoundingBox().inflate(28.0D))) {
            Vec3 toward = stalker.position().subtract(player.getEyePosition()).normalize();
            if (look.dot(toward) > 0.90D && player.hasLineOfSight(stalker)) {
                stalker.discard();
                player.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.POOF,
                        stalker.getX(), stalker.getY() + 1, stalker.getZ(), 16, 0.4D, 0.8D, 0.4D, 0.02D);
            }
        }
    }

    @SubscribeEvent
    public static void preventDeath(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && player.level().dimension().equals(LaFalsaAlucinacion.FALSE_REALITY)
                && player.getPersistentData().getBoolean(ACTIVE)) {
            // Leave enough health for the hit to register; the player regenerates on the next server tick.
            event.setAmount(Math.min(event.getAmount(), Math.max(0.0F, player.getHealth() - 1.0F)));
        }
    }

    @SubscribeEvent
    public static void preventNaturalSpawns(LivingSpawnEvent.CheckSpawn event) {
        if (event.getLevel().dimension().equals(LaFalsaAlucinacion.FALSE_REALITY)
                && !(event.getEntity() instanceof StalkerEntity) && !(event.getEntity() instanceof ChestCrawlerEntity)) {
            event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void transformLogs(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !level.dimension().equals(LaFalsaAlucinacion.FALSE_REALITY)) return;
        if (event.getState().is(BlockTags.LOGS)) {
            event.setCanceled(true);
            BlockPos pos = event.getPos();
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    new ItemStack(Items.OAK_PLANKS, 4));
            level.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.7F, 0.6F);
        }
        if (event.getState().is(FalsaBlocks.BARRERA_METALICA.get())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void breakBarrierWithGlitchPick(PlayerInteractEvent.LeftClickBlock event) {
        if (!event.getLevel().dimension().equals(LaFalsaAlucinacion.FALSE_REALITY)
                || !event.getLevel().getBlockState(event.getPos()).is(FalsaBlocks.BARRERA_METALICA.get())) return;
        event.setCanceled(true);
        if (event.getEntity() instanceof ServerPlayer player && player.getItemInHand(event.getHand()).is(FalsaItems.PICO_GLITCH.get())) {
            event.getLevel().setBlock(event.getPos(), Blocks.AIR.defaultBlockState(), 3);
            if (event.getLevel() instanceof ServerLevel level) {
                level.playSound(null, event.getPos(), SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.BLOCKS, 1.5F, 0.45F);
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL,
                        event.getPos().getX() + 0.5D, event.getPos().getY() + 0.5D, event.getPos().getZ() + 0.5D, 24, 0.3D, 0.4D, 0.3D, 0.03D);
                player.getItemInHand(event.getHand()).hurtAndBreak(1, player, p -> p.broadcastBreakEvent(event.getHand()));
            }
        }
    }

    @SubscribeEvent
    public static void invertCrafting(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        boolean corrupted = player.getPersistentData().getBoolean(ACTIVE)
                && player.level().dimension().equals(LaFalsaAlucinacion.FALSE_REALITY);
        ItemStack output = event.getCrafting();
        boolean fourSticks = event.getInventory().getContainerSize() >= 4;
        int stickCount = 0;
        int plankCount = 0;
        for (int i = 0; i < event.getInventory().getContainerSize(); i++) {
            ItemStack ingredient = event.getInventory().getItem(i);
            if (ingredient.isEmpty()) continue;
            if (ingredient.is(Items.STICK)) stickCount += ingredient.getCount();
            if (ingredient.is(ItemTags.PLANKS)) plankCount += ingredient.getCount();
        }
        fourSticks = fourSticks && stickCount >= 4 && plankCount == 0;

        if (output.is(Items.CRAFTING_TABLE) && fourSticks && !corrupted) {
            replaceCraftOutput(player, output, new ItemStack(Items.STICK, 4));
        } else if (corrupted && output.is(Items.CRAFTING_TABLE) && plankCount >= 4) {
            replaceCraftOutput(player, output, new ItemStack(Items.STICK, 4));
        } else if (corrupted && output.is(Items.STICK)) {
            replaceCraftOutput(player, output, new ItemStack(Items.CRAFTING_TABLE, 1));
        }
    }

    private static void replaceCraftOutput(Player player, ItemStack original, ItemStack replacement) {
        // ItemCraftedEvent runs while the crafted stack is being taken from the result slot.
        // Empty it before inserting the inverted product, so the normal output is not duplicated.
        original.setCount(0);
        if (!player.getInventory().add(replacement)) player.drop(replacement, false);
    }

    @SubscribeEvent
    public static void showGemLore(ItemTooltipEvent event) {
        if (event.getItemStack().is(FalsaItems.GEMA_LIBERTAD.get())) {
            event.getToolTip().add(Component.literal("¿Quieres ser libre?").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC));
            event.getToolTip().add(Component.literal("Las partículas parecen recordar un camino.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        } else if (event.getItemStack().is(FalsaItems.RUIGH_GLITCHEADO.get())) {
            event.getToolTip().add(Component.literal("[ DATOS CORRUPTOS ]").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        }
    }

    @SubscribeEvent
    public static void hugeCreeperExplosion(ExplosionEvent.Detonate event) {
        if (!(event.getExplosion().getExploder() instanceof Creeper creeper)
                || !creeper.level().dimension().equals(LaFalsaAlucinacion.FALSE_REALITY)
                || creeper.level().isClientSide) return;
        ServerLevel level = (ServerLevel) creeper.level();
        int cx = Mth.floor(creeper.getX());
        int cy = Mth.floor(creeper.getY());
        int cz = Mth.floor(creeper.getZ());
        int radius = 12;
        List<BlockPos> affected = event.getAffectedBlocks();
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                if ((x - cx) * (x - cx) + (z - cz) * (z - cz) > radius * radius) continue;
                for (int y = Math.max(1, cy - 5); y <= cy + 5; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!level.isEmptyBlock(pos)) affected.add(pos);
                }
            }
        }
    }
}
