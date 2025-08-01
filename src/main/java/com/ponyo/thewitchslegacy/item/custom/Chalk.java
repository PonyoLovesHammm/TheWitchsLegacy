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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

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
        //This calculates the block position ABOVE the clicked block (where the glyph will be placed)
        BlockPos placePos = clickedPos.above();
        //The side of the block the player clicked on (top, side, down, etc.)
        Direction face = context.getClickedFace();
        //The chalk items being used (white, red, gold, or purple)
        Item usedItem = context.getItemInHand().getItem();


        //This if statement only allows the chalk to work if the player clicked ontop of a block
        if (face != Direction.UP) {
            return InteractionResult.FAIL;
        }

        //if the block above is not replaceable,(like water, tall grass) fail & don't continue
        if (!level.getBlockState(placePos).canBeReplaced()) {
            return InteractionResult.FAIL;
        }

        //Makes sure the block clicked has a solid top face (glyphs cant be placed on leaves, etc.)
        if (!level.getBlockState(clickedPos).isFaceSturdy(level, clickedPos, Direction.UP)) {
            return InteractionResult.FAIL;
        }

        //Ensure the following block-placement logic only runs on the server side
        if (!level.isClientSide) {
            //picks a random glyph variant between 0-11
            int variant = level.getRandom().nextInt(12);

            //if using white chalk, place a white glyph block with a random variant
            if (usedItem == ModItems.WHITE_CHALK.get()) {
                level.setBlock(placePos, ModBlocks.WHITE_GLYPH.get().defaultBlockState().setValue(Glyph.VARIANT, variant), 3);
                //if using golden chalk, only return the variant 0 (that's all that exists)
            } else if (usedItem == ModItems.GOLDEN_CHALK.get()) {
                level.setBlock(placePos, ModBlocks.GOLDEN_GLYPH.get().defaultBlockState().setValue(Glyph.VARIANT, 0), 3);
                //if using red chalk, place a red glyph block with a random variant
            } else if (usedItem == ModItems.FIERY_CHALK.get()) {
                level.setBlock(placePos, ModBlocks.FIERY_GLYPH.get().defaultBlockState().setValue(Glyph.VARIANT, variant), 3);
                //if using purple chalk, place a purple glyph block with a random variant
            } else if (usedItem == ModItems.OTHERWHERE_CHALK.get()) {
                level.setBlock(placePos, ModBlocks.OTHERWHERE_GLYPH.get().defaultBlockState().setValue(Glyph.VARIANT, variant), 3);
            }
            //damage the chalk durability by 1, and if the items breaks, triggers the items breaking
            context.getItemInHand().hurtAndBreak(1, ((ServerLevel) level), context.getPlayer(),
                    item -> context.getPlayer().onEquippedItemBroken(item, EquipmentSlot.MAINHAND));

            // we pass it null to play for all nearby players, but you can pass specific players (hallucination curse?)
            level.playSound(null, placePos, ModSounds.CHALK_DRAW.get(), SoundSource.BLOCKS);
        }
        return InteractionResult.SUCCESS;
    }
}
