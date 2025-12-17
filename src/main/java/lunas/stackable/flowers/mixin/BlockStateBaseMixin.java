package lunas.stackable.flowers.mixin;

import lunas.stackable.flowers.blocks.StackableFlower;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {

    @Inject(method = "getOffset", at = @At("HEAD"), cancellable = true)
    private void lunas$customOffset(BlockPos blockPos, CallbackInfoReturnable<Vec3> cir) {
        BlockState state = (BlockState)(Object)this;
        Block block = state.getBlock();

        if (!(block instanceof StackableFlower stackableFlower)) {
            return;
        }

        if (!state.hasProperty(StackableFlower.LUNAS_FLOWER_STACKS)) {
            return;
        }

        if (stackableFlower.hasStacks(state)) {
            cir.setReturnValue(Vec3.ZERO);
            cir.cancel();
        }
    }
}
