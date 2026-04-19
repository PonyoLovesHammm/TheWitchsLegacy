package com.ponyo.thewitchslegacy.block.custom;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

public final class VoxelShapeUtils {
    private VoxelShapeUtils() {
    }

    public static Map<Direction, VoxelShape> horizontalFromNorth(VoxelShape northShape) {
        Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
        shapes.put(Direction.NORTH, northShape);
        shapes.put(Direction.EAST, rotateHorizontal(northShape, 1));
        shapes.put(Direction.SOUTH, rotateHorizontal(northShape, 2));
        shapes.put(Direction.WEST, rotateHorizontal(northShape, 3));
        return shapes;
    }

    public static Map<Direction, VoxelShape> horizontalFromSouth(VoxelShape southShape) {
        Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
        shapes.put(Direction.SOUTH, southShape);
        shapes.put(Direction.WEST, rotateHorizontal(southShape, 1));
        shapes.put(Direction.NORTH, rotateHorizontal(southShape, 2));
        shapes.put(Direction.EAST, rotateHorizontal(southShape, 3));
        return shapes;
    }

    public static VoxelShape getHorizontalShape(Map<Direction, VoxelShape> shapes, Direction direction, Direction fallback) {
        return shapes.getOrDefault(direction, shapes.get(fallback));
    }

    public static VoxelShape rotateHorizontal(VoxelShape shape, int quarterTurns) {
        VoxelShape rotated = shape;
        for (int i = 0; i < quarterTurns; i++) {
            VoxelShape current = rotated;
            final VoxelShape[] next = {Shapes.empty()};
            current.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                    next[0] = Shapes.or(
                            next[0],
                            Shapes.box(1.0 - maxZ, minY, minX, 1.0 - minZ, maxY, maxX)
                    )
            );
            rotated = next[0];
        }
        return rotated;
    }
}
