package lunas.stackable.flowers.mixin;

import lunas.stackable.flowers.blocks.StackableFlower;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {

    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    public void lunas$place(BlockPlaceContext blockPlaceContext, CallbackInfoReturnable<InteractionResult> cir) {
        Block block = ((BlockItem) (Object) this).getBlock();
        if (!(block instanceof StackableFlower stackableFlower)) {
            return;
        }

        Level level = blockPlaceContext.getLevel();
        BlockPos clickedPos = blockPlaceContext.getClickedPos();
        Direction face = blockPlaceContext.getClickedFace();
        BlockPos hitPos = clickedPos.relative(face.getOpposite());

        BlockPos stackPos;
        if (level.getBlockState(hitPos).is(block)) {
            stackPos = hitPos;
        } else if (level.getBlockState(clickedPos).is(block)) {
            stackPos = clickedPos;
        } else {
            return;
        }
        BlockState existing = level.getBlockState(stackPos);

        if (!stackableFlower.canStackMore(existing)) {
            cir.setReturnValue(InteractionResult.FAIL);
            cir.cancel();
            return;
        }

        if (level.isClientSide()) {
            cir.setReturnValue(InteractionResult.SUCCESS);
            cir.cancel();
            return;
        }

        BlockState next;
        if (!stackableFlower.hasStacks(existing)) {
            Direction facing = blockPlaceContext.getHorizontalDirection();
            next = stackableFlower.increaseStack(existing)
                    .setValue(StackableFlower.LUNAS_FACING, facing);
        } else {
            next = stackableFlower.increaseStack(existing);
        }

        Player player = blockPlaceContext.getPlayer();
        ItemStack itemStack = blockPlaceContext.getItemInHand();

        level.setBlock(stackPos, next, 3);
        next.getBlock().setPlacedBy(level, stackPos, next, player, itemStack);

        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.PLACED_BLOCK.trigger(serverPlayer, stackPos, itemStack);
        }

        SoundType soundType = next.getSoundType();
        level.playSound(
                null,
                stackPos,
                soundType.getPlaceSound(),
                SoundSource.BLOCKS,
                (soundType.getVolume() + 1.0F) / 2.0F,
                soundType.getPitch() * 0.8F
        );

        level.gameEvent(GameEvent.BLOCK_PLACE, stackPos, GameEvent.Context.of(player, next));

        if (player != null && !player.isCreative()) {
            itemStack.shrink(1);
        }

        cir.setReturnValue(InteractionResult.SUCCESS);
        cir.cancel();
    }
}
