package com.ponyo.thewitchslegacy.block.entity;

import com.ponyo.thewitchslegacy.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BambooSaplingBlock;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluids;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public final class AltarPowerSources {
    public static final int INTAKE_RADIUS = 16;
    public static final int MAX_TOTAL_POWER = 4500;
    private static final int MAX_WATER_CONTRIBUTORS = 50;
    private static final int INTAKE_RADIUS_SQUARED = INTAKE_RADIUS * INTAKE_RADIUS;

    private static final List<SourceCategory> CATEGORIES = List.of(
            category("dirt", 1, 60, blocks(Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.ROOTED_DIRT)),
            category("farmland", 1, 60, blocks(Blocks.FARMLAND)),
            category("mycelium", 1, 60, blocks(Blocks.MYCELIUM)),
            category("podzol", 1, 60, blocks(Blocks.PODZOL)),
            category("kelp", 1, 20, blocks(Blocks.KELP, Blocks.KELP_PLANT)),
            category("lily_pad", 2, 20, blocks(Blocks.LILY_PAD)),
            category("grass_block", 2, 60, blocks(Blocks.GRASS_BLOCK)),
            category("vanilla_tree_logs", 2, 100, blocks(
                    Blocks.OAK_LOG, Blocks.SPRUCE_LOG, Blocks.BIRCH_LOG, Blocks.JUNGLE_LOG,
                    Blocks.ACACIA_LOG, Blocks.DARK_OAK_LOG, Blocks.CHERRY_LOG, Blocks.MANGROVE_LOG, Blocks.PALE_OAK_LOG
            )),
            category("vines", 2, 30, blocks(Blocks.VINE)),
            category("sweet_berries", 2, 20, blocks(Blocks.SWEET_BERRY_BUSH)),
            category("glow_berries", 2, 20, state ->
                    (state.is(Blocks.CAVE_VINES) || state.is(Blocks.CAVE_VINES_PLANT))
                            && state.hasProperty(CaveVines.BERRIES)),
            category("pumpkin_block", 2, 10, blocks(Blocks.PUMPKIN)),
            category("melon_block", 2, 10, blocks(Blocks.MELON)),
            category("sea_pickle", 2, 10, blocks(Blocks.SEA_PICKLE)),
            category("melon_pumpkin_stem", 2, 20, blocks(
                    Blocks.PUMPKIN_STEM, Blocks.ATTACHED_PUMPKIN_STEM,
                    Blocks.MELON_STEM, Blocks.ATTACHED_MELON_STEM
            )),
            category("moss_block", 2, 60, blocks(Blocks.MOSS_BLOCK)),
            category("short_tall_grass", 3, 25, blocks(Blocks.SHORT_GRASS, Blocks.TALL_GRASS)),
            category("short_tall_fern", 3, 25, blocks(Blocks.FERN, Blocks.LARGE_FERN)),
            category("mushrooms", 3, 30, blocks(Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM)),
            category("cactus", 3, 20, blocks(Blocks.CACTUS)),
            category("cocoa_beans", 3, 20, blocks(Blocks.COCOA)),
            category("vanilla_leaves", 3, 100, blocks(
                    Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.BIRCH_LEAVES, Blocks.JUNGLE_LEAVES,
                    Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.CHERRY_LEAVES, Blocks.MANGROVE_LEAVES, Blocks.PALE_OAK_LEAVES
            )),
            category("sugar_cane_bamboo", 3, 50, blocks(Blocks.SUGAR_CANE, Blocks.BAMBOO, Blocks.BAMBOO_SAPLING)),
            category("coral", 3, 15, blocks(
                    Blocks.TUBE_CORAL, Blocks.BRAIN_CORAL, Blocks.BUBBLE_CORAL, Blocks.FIRE_CORAL, Blocks.HORN_CORAL
            )),
            category("coral_fan", 3, 15, blocks(
                    Blocks.TUBE_CORAL_FAN, Blocks.BRAIN_CORAL_FAN, Blocks.BUBBLE_CORAL_FAN, Blocks.FIRE_CORAL_FAN, Blocks.HORN_CORAL_FAN,
                    Blocks.TUBE_CORAL_WALL_FAN, Blocks.BRAIN_CORAL_WALL_FAN, Blocks.BUBBLE_CORAL_WALL_FAN, Blocks.FIRE_CORAL_WALL_FAN, Blocks.HORN_CORAL_WALL_FAN
            )),
            category("coral_block", 3, 10, blocks(
                    Blocks.TUBE_CORAL_BLOCK, Blocks.BRAIN_CORAL_BLOCK, Blocks.BUBBLE_CORAL_BLOCK, Blocks.FIRE_CORAL_BLOCK, Blocks.HORN_CORAL_BLOCK
            )),
            category("mushroom_block", 3, 40, blocks(Blocks.BROWN_MUSHROOM_BLOCK, Blocks.RED_MUSHROOM_BLOCK, Blocks.MUSHROOM_STEM)),
            category("flowers", 4, 60, state ->
                    (state.is(BlockTags.SMALL_FLOWERS) || state.is(Blocks.SUNFLOWER) || state.is(Blocks.LILAC)
                            || state.is(Blocks.ROSE_BUSH) || state.is(Blocks.PEONY))
                            && isSingleCountPlantState(state)
                            && !state.is(Blocks.TORCHFLOWER)
                            && !state.is(Blocks.PITCHER_CROP)
                            && !state.is(Blocks.PITCHER_PLANT)),
            category("vanilla_sapling", 4, 20, blocks(
                    Blocks.OAK_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.BIRCH_SAPLING, Blocks.JUNGLE_SAPLING,
                    Blocks.ACACIA_SAPLING, Blocks.DARK_OAK_SAPLING, Blocks.CHERRY_SAPLING, Blocks.PALE_OAK_SAPLING
            )),
            category("vanilla_crops", 4, 70, blocks(Blocks.WHEAT, Blocks.BEETROOTS, Blocks.POTATOES, Blocks.CARROTS)),
            category("azalea", 4, 10, blocks(Blocks.AZALEA, Blocks.FLOWERING_AZALEA)),
            category("torchflower", 5, 3, blocks(Blocks.TORCHFLOWER)),
            category("pitcher_plant", 5, 3, state ->
                    (state.is(Blocks.PITCHER_CROP) || state.is(Blocks.PITCHER_PLANT))
                            && isSingleCountPlantState(state)),
            category("spore_blossom", 5, 2, blocks(Blocks.SPORE_BLOSSOM)),
            category("amethyst_cluster", 5, 5, blocks(Blocks.AMETHYST_CLUSTER)),
            category("froglight", 5, 4, blocks(Blocks.OCHRE_FROGLIGHT, Blocks.VERDANT_FROGLIGHT, Blocks.PEARLESCENT_FROGLIGHT)),
            category("bee_nests", 10, 1, blocks(Blocks.BEE_NEST, Blocks.BEEHIVE)),
            category("dragon_egg", 250, 1, blocks(Blocks.DRAGON_EGG)),
            category("glint_weed", 2, 10, blocks(ModBlocks.GLINT_WEED.get())),
            category("wispy_cotton", 2, 20, state -> false),
            category("spanish_moss", 3, 25, blocks(ModBlocks.SPANISH_MOSS.get())),
            category("witchery_tree_logs", 3, 100, blocks(
                    ModBlocks.ROWAN_LOG.get(), ModBlocks.HAWTHORN_LOG.get(), ModBlocks.WILLOW_LOG.get()
            )),
            category("witchery_leaves", 4, 100, blocks(
                    ModBlocks.ROWAN_LEAVES.get(), ModBlocks.HAWTHORN_LEAVES.get(),
                    ModBlocks.WILLOW_CANOPY_LEAVES.get(), ModBlocks.WILLOW_LEAVES.get()
            )),
            category("witchery_sapling", 4, 20, blocks(
                    ModBlocks.ROWAN_SAPLING.get(), ModBlocks.HAWTHORN_SAPLING.get(), ModBlocks.WILLOW_SAPLING.get()
            )),
            category("witchery_crops", 4, 100, blocks(
                    ModBlocks.BELLADONA_PLANT.get(), ModBlocks.WATER_ARTICHOKE_CROP.get(), ModBlocks.MANDRAKE_CROP.get(),
                    ModBlocks.GARLIC_CROP.get(), ModBlocks.SNOWBELL_CROP.get()
            )),
            category("ember_moss", 4, 10, blocks(ModBlocks.EMBER_MOSS.get())),
            category("demon_heart", 40, 2, state -> false)
    );

    private AltarPowerSources() {
    }

    public static int scanNearbyPower(Level level, BlockPos centerPos) {
        int totalPower = 0;
        int[] contributors = new int[CATEGORIES.size()];
        int waterContributors = 0;

        for (BlockPos scanPos : BlockPos.betweenClosed(
                centerPos.offset(-INTAKE_RADIUS, -INTAKE_RADIUS, -INTAKE_RADIUS),
                centerPos.offset(INTAKE_RADIUS, INTAKE_RADIUS, INTAKE_RADIUS))) {
            if (scanPos.distSqr(centerPos) > INTAKE_RADIUS_SQUARED) {
                continue;
            }

            BlockState state = level.getBlockState(scanPos);
            for (int i = 0; i < CATEGORIES.size(); i++) {
                SourceCategory category = CATEGORIES.get(i);
                if (contributors[i] >= category.maxContributors()) {
                    continue;
                }
                if (!category.matcher().test(state)) {
                    continue;
                }

                contributors[i]++;
                totalPower += category.powerPerBlock();
                break;
            }

            if (waterContributors < MAX_WATER_CONTRIBUTORS && state.getFluidState().getType() == Fluids.WATER && state.getFluidState().isSource()) {
                waterContributors++;
                totalPower += 1;
            }
        }

        return totalPower;
    }

    private static SourceCategory category(String id, int powerPerBlock, int maxContributors, Predicate<BlockState> matcher) {
        return new SourceCategory(id, powerPerBlock, maxContributors, matcher);
    }

    private static Predicate<BlockState> blocks(Block... blocks) {
        Set<Block> blockSet = Set.of(blocks);
        return state -> blockSet.contains(state.getBlock());
    }

    private static boolean isSingleCountPlantState(BlockState state) {
        if (!state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            return true;
        }

        return state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER;
    }

    private record SourceCategory(String id, int powerPerBlock, int maxContributors, Predicate<BlockState> matcher) {
    }
}
