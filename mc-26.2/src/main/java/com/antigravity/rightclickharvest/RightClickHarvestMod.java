package com.antigravity.rightclickharvest;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.List;

public class RightClickHarvestMod implements ModInitializer {
    @Override
    public void onInitialize() {
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (level.isClientSide() || player.isSpectator()) {
                return InteractionResult.PASS;
            }

            // Only process the main hand to avoid duplicate trigger
            if (hand != InteractionHand.MAIN_HAND) {
                return InteractionResult.PASS;
            }

            BlockPos pos = hitResult.getBlockPos();
            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();

            // Check Melons and Pumpkins first
            if (block == Blocks.MELON || block == Blocks.PUMPKIN) {
                level.destroyBlock(pos, true, player);
                return InteractionResult.SUCCESS;
            }

            // Check Sugar Cane and Cactus (Vertical Crops)
            if (block == Blocks.SUGAR_CANE || block == Blocks.CACTUS) {
                if (harvestVerticalCrop(level, pos, block, player)) {
                    return InteractionResult.SUCCESS;
                }
            }

            // Check Crops
            if (block instanceof CropBlock cropBlock) {
                if (cropBlock.isMaxAge(state)) {
                    IntegerProperty ageProperty = getAgeProperty(state);
                    if (ageProperty != null) {
                        harvestCrop(level, pos, state, cropBlock, ageProperty, getSeedItem(block), player, hand);
                        return InteractionResult.SUCCESS;
                    }
                }
            } else if (block instanceof CocoaBlock cocoaBlock) {
                if (state.getValue(CocoaBlock.AGE) >= CocoaBlock.MAX_AGE) {
                    harvestCrop(level, pos, state, cocoaBlock, CocoaBlock.AGE, Items.COCOA_BEANS, player, hand);
                    return InteractionResult.SUCCESS;
                }
            } else if (block instanceof NetherWartBlock netherWartBlock) {
                if (state.getValue(NetherWartBlock.AGE) >= 3) {
                    harvestCrop(level, pos, state, netherWartBlock, NetherWartBlock.AGE, Items.NETHER_WART, player, hand);
                    return InteractionResult.SUCCESS;
                }
            }

            return InteractionResult.PASS;
        });
    }

    private boolean harvestVerticalCrop(Level level, BlockPos pos, Block block, Player player) {
        // Find the base of the column
        BlockPos basePos = pos;
        while (level.getBlockState(basePos.below()).getBlock() == block) {
            basePos = basePos.below();
        }

        // Break all blocks above the base
        boolean harvested = false;
        BlockPos harvestPos = basePos.above();
        while (level.getBlockState(harvestPos).getBlock() == block) {
            level.destroyBlock(harvestPos, true, player);
            harvested = true;
            harvestPos = harvestPos.above();
        }

        return harvested;
    }

    private IntegerProperty getAgeProperty(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            if (property.getName().equals("age") && property instanceof IntegerProperty) {
                return (IntegerProperty) property;
            }
        }
        return null;
    }

    private void harvestCrop(Level level, BlockPos pos, BlockState state, Block block, IntegerProperty ageProperty, Item seedItem, Player player, InteractionHand hand) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        // Get dropped stacks
        List<ItemStack> drops = Block.getDrops(state, serverLevel, pos, null, player, player.getItemInHand(hand));

        // Decrement one seed item to simulate replanting
        boolean seedConsumed = false;
        for (ItemStack drop : drops) {
            if (drop.getItem() == seedItem) {
                drop.shrink(1);
                seedConsumed = true;
                break;
            }
        }

        // Replant the crop by resetting age to 0
        level.setBlock(pos, state.setValue(ageProperty, 0), 3);

        // Play break sound and spawn particles
        level.levelEvent(2001, pos, Block.getId(state));

        // Spawn the remaining drops
        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
                Block.popResource(level, pos, drop);
            }
        }
    }

    private Item getSeedItem(Block block) {
        if (block == Blocks.WHEAT) return Items.WHEAT_SEEDS;
        if (block == Blocks.CARROTS) return Items.CARROT;
        if (block == Blocks.POTATOES) return Items.POTATO;
        if (block == Blocks.BEETROOTS) return Items.BEETROOT_SEEDS;
        if (block == Blocks.TORCHFLOWER_CROP) return Items.TORCHFLOWER_SEEDS;
        if (block == Blocks.PITCHER_CROP) return Items.PITCHER_POD;
        return Items.AIR;
    }
}
