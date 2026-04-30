package com.ponyo.thewitchslegacy.item.custom;

import com.ponyo.thewitchslegacy.block.ModBlocks;
import com.ponyo.thewitchslegacy.block.custom.Glyph;
import com.ponyo.thewitchslegacy.item.ModItems;
import com.ponyo.thewitchslegacy.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

//Defines a new Item class of Chalk which will behave like any items unless an override functionality is used
public class Chalk extends Item {
    //Constructor that takes in items properties and passes them to the Item class to initialize them (ModItems class)
    public Chalk(Properties properties) { super(properties); }

    //Overrides the use on method that is called on when a player right clicks with a chalk-items
    @Override
    public InteractionResult useOn(UseOnContext context) {
        //grabbing context info:
        //level is the world/dimension the player is
        Level level = context.getLevel();
        //the clicked Position is the position of the block the player clicked on
        BlockPos clickedPos = context.getClickedPos();
        //The side of the block the player clicked on (top, side, down, etc.)
        Direction face = context.getClickedFace();
        //The chalk items being used (white, red, gold, or purple)
        Item usedItem = context.getItemInHand().getItem();
        BlockState clickedState = level.getBlockState(clickedPos);
        boolean replacingGlyph = clickedState.getBlock() instanceof Glyph;
        BlockPos placePos = replacingGlyph ? clickedPos : clickedPos.above();


        //This if statement only allows the chalk to work if the player clicked ontop of a block
        if (face != Direction.UP) {
            return InteractionResult.FAIL;
        }

        //if the block above is not replaceable,(like water, tall grass) fail & don't continue
        if (!replacingGlyph && !level.getBlockState(placePos).canBeReplaced()) {
            return InteractionResult.FAIL;
        }

        //Makes sure the block clicked has a solid top face (glyphs cant be placed on leaves, etc.)
        BlockPos supportPos = replacingGlyph ? clickedPos.below() : clickedPos;
        if (!level.getBlockState(supportPos).isFaceSturdy(level, supportPos, Direction.UP)) {
            return InteractionResult.FAIL;
        }

        //Ensure the following block-placement logic only runs on the server side
        if (!level.isClientSide()) {
            //picks a random glyph variant between 0-11
            int variant = nextVariant(level, clickedState, replacingGlyph, usedItem);
            BlockState glyphState = glyphStateForItem(usedItem, variant);
            if (glyphState != null) {
                level.setBlock(placePos, glyphState, 3);
            }
            //damage the chalk durability by 1, and if the items breaks, triggers the items breaking
            Player player = context.getPlayer();
            EquipmentSlot slot = context.getHand() == net.minecraft.world.InteractionHand.OFF_HAND
                    ? EquipmentSlot.OFFHAND
                    : EquipmentSlot.MAINHAND;
            context.getItemInHand().hurtAndBreak(1, ((ServerLevel) level), context.getPlayer(),
                    item -> {
                        if (player != null) {
                            player.onEquippedItemBroken(item, slot);
                        }
                    });

            // we pass it null to play for all nearby players, but you can pass specific players (hallucination curse?)
            level.playSound(null, placePos, ModSounds.CHALK_DRAW.get(), SoundSource.BLOCKS);
        }
        return InteractionResult.SUCCESS;
    }

    private static int nextVariant(Level level, BlockState clickedState, boolean replacingGlyph, Item usedItem) {
        Block glyphBlock = glyphBlockForItem(usedItem);
        if (glyphBlock == ModBlocks.GOLDEN_GLYPH.get()) {
            return 0;
        }

        int variant = level.getRandom().nextInt(12);
        if (replacingGlyph && clickedState.is(glyphBlock) && clickedState.hasProperty(Glyph.VARIANT)) {
            int previousVariant = clickedState.getValue(Glyph.VARIANT);
            variant = (previousVariant + 1 + level.getRandom().nextInt(11)) % 12;
        }
        return variant;
    }

    public static boolean isTransformChalk(ItemStack stack) {
        Item item = stack.getItem();
        return item == ModItems.WHITE_CHALK.get()
                || item == ModItems.FIERY_CHALK.get()
                || item == ModItems.OTHERWHERE_CHALK.get();
    }

    public static BlockState glyphStateForStack(ItemStack stack, int variant) {
        return glyphStateForItem(stack.getItem(), variant);
    }

    public static Block glyphBlockForStack(ItemStack stack) {
        Item item = stack.getItem();
        if (item == ModItems.WHITE_CHALK.get()) {
            return ModBlocks.WHITE_GLYPH.get();
        }
        if (item == ModItems.GOLDEN_CHALK.get()) {
            return ModBlocks.GOLDEN_GLYPH.get();
        }
        if (item == ModItems.FIERY_CHALK.get()) {
            return ModBlocks.FIERY_GLYPH.get();
        }
        if (item == ModItems.OTHERWHERE_CHALK.get()) {
            return ModBlocks.OTHERWHERE_GLYPH.get();
        }
        return null;
    }

    private static BlockState glyphStateForItem(Item item, int variant) {
        Block glyphBlock = glyphBlockForItem(item);
        if (glyphBlock == null) {
            return null;
        }

        int clampedVariant = glyphBlock == ModBlocks.GOLDEN_GLYPH.get() ? 0 : Math.max(0, Math.min(variant, 11));
        return glyphBlock.defaultBlockState().setValue(Glyph.VARIANT, clampedVariant);
    }

    private static Block glyphBlockForItem(Item item) {
        if (item == ModItems.WHITE_CHALK.get()) {
            return ModBlocks.WHITE_GLYPH.get();
        }
        if (item == ModItems.GOLDEN_CHALK.get()) {
            return ModBlocks.GOLDEN_GLYPH.get();
        }
        if (item == ModItems.FIERY_CHALK.get()) {
            return ModBlocks.FIERY_GLYPH.get();
        }
        if (item == ModItems.OTHERWHERE_CHALK.get()) {
            return ModBlocks.OTHERWHERE_GLYPH.get();
        }
        return null;
    }
}
