package lunas.stackable.flowers.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Set;

public interface StackableFlower {
    int MIN_STACKS = 1;
    int MAX_STACKS = 6;

    Set<String> LUNAS_BONEMEAL_EXCEPTION_BLOCK_NAMES = Set.of(
            "block.minecraft.wither_rose",
            "block.minecraft.torchflower"
    );

    VoxelShape LUNAS_STACKED = Block.box(2.0, 0.0, 2.0, 14.0, 5.0, 14.0);

    IntegerProperty LUNAS_FLOWER_STACKS = IntegerProperty.create("lunas_flower_stacks_prop", MIN_STACKS, MAX_STACKS);
    EnumProperty<Direction> LUNAS_FACING = EnumProperty.create("lunas_facing", Direction.class);
    BooleanProperty LUNAS_IS_BONEMEALED = BooleanProperty.create("lunas_is_bonemealed");
    BooleanProperty LUNAS_IS_SHEARED = BooleanProperty.create("lunas_is_sheared");

    default boolean isBonemealException(BlockState blockState) {
        return LUNAS_BONEMEAL_EXCEPTION_BLOCK_NAMES.contains(blockState.getBlock().getDescriptionId());
    }

    default boolean canStackMore(BlockState blockState) {
        int currentStacks = blockState.getValue(LUNAS_FLOWER_STACKS);
        return currentStacks < MAX_STACKS;
    }

    default boolean hasStacks(BlockState blockState) {
        int currentStacks = blockState.getValue(LUNAS_FLOWER_STACKS);
        return currentStacks > MIN_STACKS;
    }

    default BlockState increaseStack(BlockState blockState) {
        int currentStacks = blockState.getValue(LUNAS_FLOWER_STACKS);
        if (currentStacks < MAX_STACKS) {
            return blockState.setValue(LUNAS_FLOWER_STACKS, currentStacks + 1);
        }
        return blockState;
    }

    default BlockState decreaseStack(BlockState blockState, Level serverLevel, BlockPos blockPos) {
        int currentStacks = blockState.getValue(LUNAS_FLOWER_STACKS);
        if (currentStacks > MIN_STACKS) {
            blockState = blockState.setValue(LUNAS_FLOWER_STACKS, currentStacks - 1);
        }
        Block.popResource(serverLevel, blockPos, new ItemStack((ItemLike) this));
        return blockState;
    }

    default VoxelShape getStackShape() {
        return LUNAS_STACKED;
    }

    default boolean isSheared(BlockState blockState) {
        return blockState.getValue(LUNAS_IS_SHEARED);
    }

    default BlockState toggleIsSheared(BlockState blockState) {
        boolean current = blockState.getValue(LUNAS_IS_SHEARED);
        return blockState.setValue(LUNAS_IS_SHEARED, !current);
    }

    default boolean isBonemealed(BlockState blockState) {
        return blockState.getValue(LUNAS_IS_BONEMEALED);
    }

    default BlockState toggleIsBonemealed(BlockState blockState) {
        boolean current = blockState.getValue(LUNAS_IS_BONEMEALED);
        return blockState.setValue(LUNAS_IS_BONEMEALED, !current);
    }

    default BlockState updateDirection(BlockState blockState, Direction direction) {
        return blockState.setValue(LUNAS_FACING, direction);
    }

    static BlockState defaultBlockState(BlockState to) {
        return to.setValue(StackableFlower.LUNAS_FACING, Direction.NORTH)
                .setValue(StackableFlower.LUNAS_IS_BONEMEALED, false)
                .setValue(StackableFlower.LUNAS_IS_SHEARED, false);
    }

    static BlockState cloneBlockState(BlockState from, BlockState to) {
        return to.setValue(StackableFlower.LUNAS_FLOWER_STACKS, from.getValue(StackableFlower.LUNAS_FLOWER_STACKS))
                .setValue(StackableFlower.LUNAS_FACING, from.getValue(StackableFlower.LUNAS_FACING))
                .setValue(StackableFlower.LUNAS_IS_BONEMEALED, from.getValue(StackableFlower.LUNAS_IS_BONEMEALED))
                .setValue(StackableFlower.LUNAS_IS_SHEARED, from.getValue(StackableFlower.LUNAS_IS_SHEARED));
    }
}
