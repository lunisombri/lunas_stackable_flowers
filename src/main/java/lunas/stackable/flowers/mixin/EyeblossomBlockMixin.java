package lunas.stackable.flowers.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import lunas.stackable.flowers.blocks.StackableFlower;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.EyeblossomBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EyeblossomBlock.class)
public class EyeblossomBlockMixin {
    @Redirect(
            method = "tryChangingState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"
            )
    )
    private boolean lunas$redirectSetBlock(
            ServerLevel level,
            BlockPos pos,
            BlockState newState,
            int flags,
            @Local(argsOnly = true) BlockState oldState
    ) {
        if (oldState.getBlock() instanceof StackableFlower) {
            newState = newState
                    .setValue(StackableFlower.LUNAS_FLOWER_STACKS,
                            oldState.getValue(StackableFlower.LUNAS_FLOWER_STACKS))
                    .setValue(StackableFlower.LUNAS_FACING,
                            oldState.getValue(StackableFlower.LUNAS_FACING))
                    .setValue(StackableFlower.LUNAS_IS_BONEMEALED,
                            oldState.getValue(StackableFlower.LUNAS_IS_BONEMEALED))
                    .setValue(StackableFlower.LUNAS_IS_SHEARED,
                            oldState.getValue(StackableFlower.LUNAS_IS_SHEARED));;
        }

        return level.setBlock(pos, newState, flags);
    }
}
