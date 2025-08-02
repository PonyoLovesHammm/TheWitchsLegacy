package com.ponyo.thewitchslegacy.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;

public class SpanishMoss extends VineBlock {
    public SpanishMoss(Properties properties) {
        super(properties.randomTicks());
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        ItemStack tool = player.getMainHandItem();
        return tool.is(Items.SHEARS);
    }
}
