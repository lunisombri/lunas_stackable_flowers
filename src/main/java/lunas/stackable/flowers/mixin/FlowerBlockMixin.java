package lunas.stackable.flowers.mixin;

import lunas.stackable.flowers.blocks.StackableFlower;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlowerBlock.class)
public class FlowerBlockMixin implements StackableFlower, BonemealableBlock {

    @Inject(method = "getShape", at = @At("HEAD"), cancellable = true)
    private void lunas$getShape(
            BlockState blockState,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context,
            CallbackInfoReturnable<VoxelShape> cir
    ) {
        if (!(blockState.getBlock() instanceof StackableFlower)) {
            return;
        }

        cir.setReturnValue(getStackShape(blockState));
        cir.cancel();
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return !isBonemealException(blockState) && (canStackMore(blockState) || isSheared(blockState) || !isBonemealed(blockState));
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        BlockState newState = blockState;
        if (isSheared(blockState)) {
            serverLevel.setBlock(blockPos, this.toggleIsSheared(blockState), 2);
            return;
        }

        if (!isBonemealed(blockState)) {
            newState = this.toggleIsBonemealed(blockState);
        }

        if (canStackMore(blockState)) {
            newState = this.increaseStack(newState);
        }

        serverLevel.setBlock(blockPos, newState, 2);
    }
}
