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

    VoxelShape LUNAS_SINGLE = Block.box(5.0, 0.0, 5.0, 11.0, 10.0, 11.0);
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

    BlockState decreaseStack(BlockState blockState, Level serverLevel, BlockPos blockPos);

    default VoxelShape getStackShape(BlockState blockState) {
        return hasStacks(blockState) ? LUNAS_STACKED : LUNAS_SINGLE;
    }

    default boolean isSheared(BlockState blockState) {
        return blockState.getValue(LUNAS_IS_SHEARED);
    }

    default boolean isBonemealed(BlockState blockState) {
        return blockState.getValue(LUNAS_IS_BONEMEALED);
    }

    default BlockState toggleIsBonemealed(BlockState blockState) {
        boolean current = blockState.getValue(LUNAS_IS_BONEMEALED);
        return blockState.setValue(LUNAS_IS_BONEMEALED, !current);
    }
}
