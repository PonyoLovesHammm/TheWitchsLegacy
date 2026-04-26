package com.ponyo.thewitchslegacy.block.entity.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public class SpinningWheelRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public float wheelRotation;
    public float bobbinRotation;
}
