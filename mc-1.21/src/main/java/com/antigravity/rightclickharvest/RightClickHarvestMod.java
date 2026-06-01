package com.antigravity.rightclickharvest;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class RightClickHarvestMod implements ModInitializer {
    @Override
    public void onInitialize() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient() || player.isSpectator()) {
                return ActionResult.PASS;
            }

            // Only process the main hand to avoid duplicate trigger
            if (hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }

            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            // Check Melons and Pumpkins first
            if (block == Blocks.MELON || block == Blocks.PUMPKIN) {
                world.breakBlock(pos, true, player);
                return ActionResult.SUCCESS;
            }

            // Check Sugar Cane and Cactus (Vertical Crops)
            if (block == Blocks.SUGAR_CANE || block == Blocks.CACTUS) {
                if (harvestVerticalCrop(world, pos, block, player)) {
                    return ActionResult.SUCCESS;
                }
            }

            // Check Crops
            if (block instanceof CropBlock cropBlock) {
                if (cropBlock.isMature(state)) {
                    IntProperty ageProperty = getAgeProperty(state);
                    if (ageProperty != null) {
                        harvestCrop(world, pos, state, cropBlock, ageProperty, getSeedItem(block), player, hand);
                        return ActionResult.SUCCESS;
                    }
                }
            } else if (block instanceof CocoaBlock cocoaBlock) {
                if (state.get(CocoaBlock.AGE) >= 2) {
                    harvestCrop(world, pos, state, cocoaBlock, CocoaBlock.AGE, Items.COCOA_BEANS, player, hand);
                    return ActionResult.SUCCESS;
                }
            } else if (block instanceof NetherWartBlock netherWartBlock) {
                if (state.get(NetherWartBlock.AGE) >= 3) {
                    harvestCrop(world, pos, state, netherWartBlock, NetherWartBlock.AGE, Items.NETHER_WART, player, hand);
                    return ActionResult.SUCCESS;
                }
            }

            return ActionResult.PASS;
        });
    }

    private boolean harvestVerticalCrop(World world, BlockPos pos, Block block, PlayerEntity player) {
        // Find the base of the column
        BlockPos basePos = pos;
        while (world.getBlockState(basePos.down()).getBlock() == block) {
            basePos = basePos.down();
        }

        // Break all blocks above the base
        boolean harvested = false;
        BlockPos harvestPos = basePos.up();
        while (world.getBlockState(harvestPos).getBlock() == block) {
            world.breakBlock(harvestPos, true, player);
            harvested = true;
            harvestPos = harvestPos.up();
        }

        return harvested;
    }

    private IntProperty getAgeProperty(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            if (property.getName().equals("age") && property instanceof IntProperty) {
                return (IntProperty) property;
            }
        }
        return null;
    }

    private void harvestCrop(World world, BlockPos pos, BlockState state, Block block, IntProperty ageProperty, net.minecraft.item.Item seedItem, PlayerEntity player, Hand hand) {
        if (!(world instanceof ServerWorld serverWorld)) return;

        // Get dropped stacks
        List<ItemStack> drops = Block.getDroppedStacks(state, serverWorld, pos, null, player, player.getStackInHand(hand));

        // Decrement one seed item to simulate replanting
        boolean seedConsumed = false;
        for (ItemStack drop : drops) {
            if (drop.getItem() == seedItem) {
                drop.decrement(1);
                seedConsumed = true;
                break;
            }
        }

        // Replant the crop by resetting age to 0
        world.setBlockState(pos, state.with(ageProperty, 0));

        // Play break sound and spawn particles
        world.syncWorldEvent(2001, pos, Block.getRawIdFromState(state));

        // Spawn the remaining drops
        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
                Block.dropStack(world, pos, drop);
            }
        }
    }

    private net.minecraft.item.Item getSeedItem(Block block) {
        if (block == Blocks.WHEAT) return Items.WHEAT_SEEDS;
        if (block == Blocks.CARROTS) return Items.CARROT;
        if (block == Blocks.POTATOES) return Items.POTATO;
        if (block == Blocks.BEETROOTS) return Items.BEETROOT_SEEDS;
        if (block == Blocks.TORCHFLOWER_CROP) return Items.TORCHFLOWER_SEEDS;
        if (block == Blocks.PITCHER_CROP) return Items.PITCHER_POD;
        return Items.AIR;
    }
}
