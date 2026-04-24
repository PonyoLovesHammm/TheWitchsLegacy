package com.ponyo.thewitchslegacy.block.entity;

import com.ponyo.thewitchslegacy.block.custom.Candelabra;
import com.ponyo.thewitchslegacy.menu.AltarMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.EnumMap;
import java.util.function.Predicate;

public class AltarBlockEntity extends BlockEntity implements MenuProvider {
    private static final int DATA_COUNT = 4;
    private static final int RECHECK_INTERVAL = 20;
    private static final int BASE_RECHARGE_RATE = 10;
    private static final int BASE_DISTRIBUTION_RANGE = 16;
    private static final List<AboveAltarModifier> ABOVE_ALTAR_MODIFIERS = List.of(
            new AboveAltarModifier(ModifierGroup.HEAD, state -> state.is(Blocks.SKELETON_SKULL), 1, 1, 0, 1),
            new AboveAltarModifier(ModifierGroup.HEAD, state -> state.is(Blocks.WITHER_SKELETON_SKULL), 2, 2, 0, 1),
            new AboveAltarModifier(ModifierGroup.HEAD, state -> state.is(Blocks.PLAYER_HEAD), 3, 3, 0, 1),
            new AboveAltarModifier(ModifierGroup.LIGHT, state -> state.is(BlockTags.CANDLES), 1, 0, 0, 1),
            new AboveAltarModifier(ModifierGroup.LIGHT, state -> state.getBlock() instanceof Candelabra, 2, 0, 0, 1),
            new AboveAltarModifier(ModifierGroup.CHALICE, state -> hasRegistryPath(state, "chalice"), 0, 1, 0, 1),
            new AboveAltarModifier(ModifierGroup.CHALICE, state -> hasRegistryPath(state, "filled_chalice"), 0, 2, 0, 1),
            new AboveAltarModifier(ModifierGroup.ARTHANA, state -> hasRegistryPath(state, "arthana"), 0, 0, 2, 1),
            new AboveAltarModifier(ModifierGroup.NONE, state -> hasRegistryPath(state, "infinity_egg"), 10, 10, 0, 1),
            new AboveAltarModifier(ModifierGroup.PENTACLE, state -> hasRegistryPath(state, "pentacle"), 0, 0, 0, 2)
    );

    private int currentPower;
    private int maxPower = 100;
    private int accumulationRate = BASE_RECHARGE_RATE;
    private int distributionRange = BASE_DISTRIBUTION_RANGE;
    private BlockPos controllerPos;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> AltarBlockEntity.this.currentPower;
                case 1 -> AltarBlockEntity.this.maxPower;
                case 2 -> AltarBlockEntity.this.accumulationRate;
                case 3 -> AltarBlockEntity.this.distributionRange;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> AltarBlockEntity.this.currentPower = value;
                case 1 -> AltarBlockEntity.this.maxPower = value;
                case 2 -> AltarBlockEntity.this.accumulationRate = value;
                case 3 -> AltarBlockEntity.this.distributionRange = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public AltarBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.ALTAR.get(), pos, blockState);
        this.controllerPos = pos;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.thewitchslegacy.altar.title");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new AltarMenu(containerId, inventory, this.dataAccess, this.getBlockPos());
    }

    public ContainerData getDataAccess() {
        return this.dataAccess;
    }

    public void openMenu(ServerPlayer player) {
        player.openMenu(this, this.getBlockPos());
    }

    public int getCurrentPower() {
        return this.currentPower;
    }

    public int getDistributionRange() {
        return this.distributionRange;
    }

    public boolean consumePower(int amount) {
        if (amount <= 0) {
            return true;
        }
        if (this.currentPower < amount) {
            return false;
        }

        this.currentPower -= amount;
        this.setChanged();
        return true;
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, AltarBlockEntity blockEntity) {
        if (!blockEntity.isController()) {
            return;
        }

        if (level.getGameTime() % RECHECK_INTERVAL != 0L) {
            return;
        }

        Set<BlockPos> multiblockPositions = getMultiblockPositions(level, pos);
        BlockPos powerScanOrigin = getPowerScanOrigin(multiblockPositions, pos);
        int nearbyPower = AltarPowerSources.scanNearbyPower(level, powerScanOrigin);
        AltarBonuses bonuses = getBonuses(level, pos);
        int modifiedMaxPower = getModifiedMaxPower(nearbyPower, bonuses.maxPowerBonusMultiplier(), bonuses.globalMultiplier());
        int newRate = getRechargeRate(nearbyPower, bonuses.rechargeBonusMultiplier(), bonuses.globalMultiplier());
        int newDistributionRange = getDistributionRange(bonuses.distributionRangeBonusMultiplier());

        boolean changed = modifiedMaxPower != blockEntity.maxPower
                || newRate != blockEntity.accumulationRate
                || newDistributionRange != blockEntity.distributionRange;
        blockEntity.maxPower = modifiedMaxPower;
        blockEntity.accumulationRate = newRate;
        blockEntity.distributionRange = newDistributionRange;

        int newCurrentPower = Math.min(blockEntity.currentPower + newRate, modifiedMaxPower);
        if (newCurrentPower != blockEntity.currentPower) {
            blockEntity.currentPower = newCurrentPower;
            changed = true;
        }

        if (blockEntity.currentPower > blockEntity.maxPower) {
            blockEntity.currentPower = blockEntity.maxPower;
            changed = true;
        }

        if (changed) {
            blockEntity.setChanged();
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("current_power", this.currentPower);
        output.putInt("max_power", this.maxPower);
        output.putInt("accumulation_rate", this.accumulationRate);
        output.putInt("distribution_range", this.distributionRange);
        output.putInt("controller_x", this.controllerPos.getX());
        output.putInt("controller_y", this.controllerPos.getY());
        output.putInt("controller_z", this.controllerPos.getZ());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.currentPower = input.getIntOr("current_power", 0);
        this.maxPower = input.getIntOr("max_power", 100);
        this.accumulationRate = input.getIntOr("accumulation_rate", BASE_RECHARGE_RATE);
        this.distributionRange = input.getIntOr("distribution_range", BASE_DISTRIBUTION_RANGE);
        this.controllerPos = new BlockPos(
                input.getIntOr("controller_x", this.worldPosition.getX()),
                input.getIntOr("controller_y", this.worldPosition.getY()),
                input.getIntOr("controller_z", this.worldPosition.getZ())
        );
    }

    public void setControllerPos(BlockPos controllerPos) {
        this.controllerPos = controllerPos.immutable();
        this.setChanged();
    }

    public BlockPos getControllerPos() {
        return this.controllerPos;
    }

    public boolean isController() {
        return this.worldPosition.equals(this.controllerPos);
    }

    private static int getRechargeRate(int nearbyPower, int rechargeBonusMultiplier, int globalMultiplier) {
        if (nearbyPower <= 0) {
            return 0;
        }

        return (BASE_RECHARGE_RATE + (BASE_RECHARGE_RATE * rechargeBonusMultiplier)) * globalMultiplier;
    }

    private static int getModifiedMaxPower(int nearbyPower, int maxPowerBonusMultiplier, int globalMultiplier) {
        if (nearbyPower <= 0) {
            return 0;
        }

        return (nearbyPower + (nearbyPower * maxPowerBonusMultiplier)) * globalMultiplier;
    }

    private static int getDistributionRange(int distributionRangeBonusMultiplier) {
        return BASE_DISTRIBUTION_RANGE + (BASE_DISTRIBUTION_RANGE * distributionRangeBonusMultiplier);
    }

    private static AltarBonuses getBonuses(ServerLevel level, BlockPos controllerPos) {
        Map<ModifierGroup, AltarBonuses> groupedBonuses = new EnumMap<>(ModifierGroup.class);

        for (BlockPos altarPos : getMultiblockPositions(level, controllerPos)) {
            BlockState blockAbove = level.getBlockState(altarPos.above());
            for (AboveAltarModifier modifier : ABOVE_ALTAR_MODIFIERS) {
                if (!modifier.matcher().test(blockAbove)) {
                    continue;
                }

                AltarBonuses candidate = new AltarBonuses(
                        modifier.rechargeBonusMultiplier(),
                        modifier.maxPowerBonusMultiplier(),
                        modifier.distributionRangeBonusMultiplier(),
                        modifier.globalMultiplier()
                );
                groupedBonuses.merge(
                        modifier.group(),
                        candidate,
                        (current, next) -> current.score() >= next.score() ? current : next
                );
            }
        }

        int rechargeBonusMultiplier = 0;
        int maxPowerBonusMultiplier = 0;
        int distributionRangeBonusMultiplier = 0;
        int globalMultiplier = 1;

        for (AltarBonuses bonus : groupedBonuses.values()) {
            rechargeBonusMultiplier += bonus.rechargeBonusMultiplier();
            maxPowerBonusMultiplier += bonus.maxPowerBonusMultiplier();
            distributionRangeBonusMultiplier += bonus.distributionRangeBonusMultiplier();
            globalMultiplier *= bonus.globalMultiplier();
        }

        return new AltarBonuses(rechargeBonusMultiplier, maxPowerBonusMultiplier, distributionRangeBonusMultiplier, globalMultiplier);
    }

    private static Set<BlockPos> getMultiblockPositions(ServerLevel level, BlockPos controllerPos) {
        Set<BlockPos> positions = new HashSet<>();
        for (int dx = 0; dx <= 2; dx++) {
            for (int dz = 0; dz <= 2; dz++) {
                BlockPos checkPos = controllerPos.offset(dx, 0, dz);
                if (!(level.getBlockEntity(checkPos) instanceof AltarBlockEntity altarBlockEntity)) {
                    continue;
                }
                if (!altarBlockEntity.getControllerPos().equals(controllerPos)) {
                    continue;
                }

                positions.add(checkPos);
            }
        }

        return positions;
    }

    private static BlockPos getPowerScanOrigin(Set<BlockPos> multiblockPositions, BlockPos fallbackPos) {
        if (multiblockPositions.isEmpty()) {
            return fallbackPos;
        }

        double averageX = 0.0;
        double averageZ = 0.0;
        for (BlockPos altarPos : multiblockPositions) {
            averageX += altarPos.getX();
            averageZ += altarPos.getZ();
        }

        averageX /= multiblockPositions.size();
        averageZ /= multiblockPositions.size();

        BlockPos bestPos = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos altarPos : multiblockPositions) {
            double dx = altarPos.getX() - averageX;
            double dz = altarPos.getZ() - averageZ;
            double distance = (dx * dx) + (dz * dz);

            if (bestPos == null
                    || distance < bestDistance
                    || (distance == bestDistance && compareNorthWestFirst(altarPos, bestPos) < 0)) {
                bestPos = altarPos;
                bestDistance = distance;
            }
        }

        return bestPos != null ? bestPos : fallbackPos;
    }

    private static int compareNorthWestFirst(BlockPos first, BlockPos second) {
        int xCompare = Integer.compare(first.getX(), second.getX());
        if (xCompare != 0) {
            return xCompare;
        }

        return Integer.compare(first.getZ(), second.getZ());
    }

    private static boolean hasRegistryPath(BlockState state, String path) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().equals(path);
    }

    private enum ModifierGroup {
        NONE,
        HEAD,
        LIGHT,
        CHALICE,
        ARTHANA,
        PENTACLE
    }

    private record AboveAltarModifier(ModifierGroup group, Predicate<BlockState> matcher, int rechargeBonusMultiplier,
                                      int maxPowerBonusMultiplier, int distributionRangeBonusMultiplier, int globalMultiplier) {
    }

    private record AltarBonuses(int rechargeBonusMultiplier, int maxPowerBonusMultiplier,
                                int distributionRangeBonusMultiplier, int globalMultiplier) {
        private int score() {
            return this.rechargeBonusMultiplier
                    + this.maxPowerBonusMultiplier
                    + this.distributionRangeBonusMultiplier
                    + (this.globalMultiplier - 1);
        }
    }
}
