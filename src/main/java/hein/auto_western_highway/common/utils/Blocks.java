package hein.auto_western_highway.common.utils;

import hein.auto_western_highway.common.types.BlocknameAndState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static hein.auto_western_highway.common.Globals.globalPlayerNonNull;
import static net.minecraft.util.math.Direction.Axis.*;

public class Blocks {
    public static BlockPos getStandingBlock(ClientPlayerEntity player) {
        return offsetBlock(getPlayerFeetBlock(player), 0, -1, 0);
    }

    public static BlockPos getPlayerFeetBlock(ClientPlayerEntity player) {
        return player.getBlockPos();
    }

    public static String getBlockId(BlockItem item) {
        return Registries.BLOCK.getId(item.getBlock()).getPath();
    }

    public static String getBlockId(Block block) {
        return Registries.BLOCK.getId(block).getPath();
    }

    public static String getBlockId(Item item) {
        assert item instanceof BlockItem;
        return getBlockId((BlockItem) item);
    }

    public static BlockPos copyBlock(BlockPos block) {
        return new BlockPos(block.getX(), block.getY(), block.getZ());
    }

    public static BlockPos copyBlock(BlockPos block, int x, int y, int z) {
        return offsetBlock(new BlockPos(block.getX(), block.getY(), block.getZ()), x, y, z);
    }

    public static BlockPos offsetBlock(BlockPos block, int x, int y, int z) {
        return block.offset(X, x).offset(Y, y).offset(Z, z);
    }

    public static boolean isScaffoldBlockingBlock(String block) {
        return (block.contains("snow") && !block.contains("block")) ||
                block.contains("_grass");
    }

    public static List<String> getBlocksNameFromBlockPositions(List<BlockPos> blockPositions) {
        return blockPositions.stream().map(block -> getBlockId(globalPlayerNonNull.get().clientWorld.getBlockState(block).getBlock())).toList();
    }

    public static List<BlocknameAndState> getBlocknamesAndStatesFromBlockPositions(List<BlockPos> blockPositions) {
        List<BlocknameAndState> blocknamesAndStates = new ArrayList<>();
        blockPositions.forEach(blockPos -> {
            BlockState state = globalPlayerNonNull.get().clientWorld.getBlockState(blockPos);
            blocknamesAndStates.add(new BlocknameAndState(getBlockId(state.getBlock()), state));
        });
        return blocknamesAndStates;
    }

    public static boolean isNonTerrainBlock(String block) {
        return Stream.of(
                "_log",
                "air",
                "cactus",
                "dandelion",
                "dead_bush",
                "fern",
                "flower",
                "leaves",
                "lilac",
                "lily_pad",
                "moss",
                "nether",
                "obsidian",
                "poppy",
                "petal",
                "portal",
                "vine"
        ).anyMatch(block::contains) || isScaffoldBlockingBlock(block);
    }
}
