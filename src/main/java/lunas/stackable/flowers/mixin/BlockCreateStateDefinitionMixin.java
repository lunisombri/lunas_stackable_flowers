package lunas.stackable.flowers.mixin;

import lunas.stackable.flowers.blocks.StackableFlower;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockCreateStateDefinitionMixin {

    @Inject(method = "createBlockStateDefinition", at = @At("HEAD"))
    private void lunas$onCreateBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        if (this instanceof StackableFlower) {
            builder.add(StackableFlower.LUNAS_FLOWER_STACKS);
            builder.add(StackableFlower.LUNAS_FACING);
            builder.add(StackableFlower.LUNAS_IS_BONEMEALED);
            builder.add(StackableFlower.LUNAS_IS_SHEARED);
        }
    }
}