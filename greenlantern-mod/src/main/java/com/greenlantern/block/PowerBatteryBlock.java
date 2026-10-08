package com.greenlantern.block;

import com.greenlantern.ability.Fx;
import com.greenlantern.ability.Oath;
import com.greenlantern.item.PowerRingItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Right-click to fully recharge every Power Ring in your inventory. */
public class PowerBatteryBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 14, 13);

    public PowerBatteryBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        int recharged = 0;
        boolean hasRing = false;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.getItem() instanceof PowerRingItem) {
                hasRing = true;
                if (PowerRingItem.getCharge(s) < PowerRingItem.MAX_CHARGE) {
                    PowerRingItem.setCharge(s, PowerRingItem.MAX_CHARGE);
                    recharged++;
                }
            }
        }

        ServerLevel sl = (ServerLevel) level;
        if (recharged > 0) {
            level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 1.4f);
            Fx.burst(sl, player.position().add(0, 1.0, 0), 40);
            player.displayClientMessage(Component.translatable("message.greenlantern.recharged")
                    .withStyle(ChatFormatting.GREEN), true);
        } else if (hasRing) {
            player.displayClientMessage(Component.translatable("message.greenlantern.full")
                    .withStyle(ChatFormatting.DARK_GREEN), true);
        } else {
            player.displayClientMessage(Component.translatable("message.greenlantern.no_ring")
                    .withStyle(ChatFormatting.GRAY), true);
        }
        if (hasRing && player instanceof ServerPlayer sp) Oath.speak(sp);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {
            level.addParticle(Fx.GREEN_DUST,
                    pos.getX() + 0.3 + random.nextDouble() * 0.4,
                    pos.getY() + 0.9 + random.nextDouble() * 0.2,
                    pos.getZ() + 0.3 + random.nextDouble() * 0.4,
                    0, 0.03, 0);
        }
    }
}
