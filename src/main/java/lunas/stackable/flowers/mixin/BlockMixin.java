package lunas.stackable.flowers.mixin;

import lunas.stackable.flowers.blocks.StackableFlower;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Block.class)
public abstract class BlockMixin {

    @ModifyVariable(
            method = "registerDefaultState",
            at = @At("HEAD"),
            argsOnly = true
    )
    private BlockState lunas$modifyDefaultState(BlockState blockState) {
        if (!(this instanceof StackableFlower)) {
            return blockState;
        }

        return StackableFlower.defaultBlockState(blockState);
    }
}
