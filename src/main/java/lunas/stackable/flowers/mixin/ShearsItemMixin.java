package lunas.stackable.flowers.mixin;

import lunas.stackable.flowers.blocks.StackableFlower;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShearsItem.class)
public class ShearsItemMixin {

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    public void lunas$useOn(UseOnContext useOnContext, CallbackInfoReturnable<InteractionResult> cir) {
        Level level = useOnContext.getLevel();
        BlockPos blockPos = useOnContext.getClickedPos();
        BlockState blockState = level.getBlockState(blockPos);

        if (!(blockState.getBlock() instanceof StackableFlower stackableFlower)) {
            return;
        }

        if (stackableFlower.isSheared(blockState)) {
            cir.setReturnValue(InteractionResult.SUCCESS);
            cir.cancel();
        }

        Player player = useOnContext.getPlayer();
        ItemStack itemStack = useOnContext.getItemInHand();

        if (level.isClientSide()) {
            cir.setReturnValue(InteractionResult.SUCCESS);
            cir.cancel();
            return;
        }

        BlockState next = blockState.setValue(StackableFlower.LUNAS_IS_SHEARED, true);
        level.setBlock(blockPos, next, 3);

        level.playSound(null, blockPos, SoundEvents.GROWING_PLANT_CROP, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(useOnContext.getPlayer(), next));

        if (player instanceof ServerPlayer) {
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)player, blockPos, itemStack);
        }

        if (player != null) {
            itemStack.hurtAndBreak(1, player, useOnContext.getHand().asEquipmentSlot());
        }

        cir.setReturnValue(InteractionResult.SUCCESS);
        cir.cancel();
    }
}
