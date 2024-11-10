package hein.auto_western_highway;

import hein.auto_western_highway.types.BlocknameAndState;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static hein.auto_western_highway.Globals.globalPlayer;
import static net.minecraft.util.math.Direction.Axis.*;

public class Blocks {
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
        return blockPositions.stream().map(block ->
                Registries.BLOCK.getId(globalPlayer.clientWorld.getBlockState(block).getBlock())
                        .toString().substring(10) // remove the "minecraft:" prefix
        ).toList();
    }

    public static List<BlocknameAndState> getBlocknamesAndStatesFromBlockPositions(List<BlockPos> blockPositions) {
        List<BlocknameAndState> blocknamesAndStates = new ArrayList<>();
        blockPositions.forEach(blockPos -> {
            BlockState state = globalPlayer.clientWorld.getBlockState(blockPos);
            blocknamesAndStates.add(new BlocknameAndState(Registries.BLOCK.getId(state.getBlock()).toString().substring(10), state));
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
                "nether",
                "obsidian",
                "poppy",
                "petal",
                "portal",
                "vine"
        ).anyMatch(block::contains) || isScaffoldBlockingBlock(block);
    }
}
