package hein.auto_western_highway;

import baritone.api.BaritoneAPI;
import baritone.api.Settings;
import baritone.api.process.IBuilderProcess;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static hein.auto_western_highway.Constants.buildIgnoreBlocks;
import static hein.auto_western_highway.Utils.waitUntilTrue;
import static net.minecraft.block.Blocks.*;

public class Baritone {
    public static void resetSettings() {
        Settings settings = BaritoneAPI.getSettings();
        settings.breakFromAbove.value = true;
        settings.goalBreakFromAbove.value = true;
        settings.buildRepeat.value = new Vec3i(0, 0, 0);
        settings.buildRepeatCount.value = 0;
        settings.buildInLayers.value = true;
        settings.layerOrder.value = true;
        settings.buildIgnoreExisting.value = false;
        settings.blocksToDisallowBreaking.value = List.of(
                SMOOTH_STONE,
                SMOOTH_STONE_SLAB,
                STONE_BRICKS,
                STONE_BRICK_SLAB
        );
        settings.buildIgnoreBlocks.value = buildIgnoreBlocks;
    }

    public static void build(AutoHighwaySchematic schematic, BlockPos buildOrigin) {
        File file = new File(new File(BaritoneAPI.getProvider().getPrimaryBaritone().getPlayerContext().minecraft().runDirectory, "schematics"), schematic.getFileName() + ".litematic").getAbsoluteFile();
        if (FilenameUtils.getExtension(file.getAbsolutePath()).isEmpty()) {
            file = new File(file.getAbsolutePath() + "." + BaritoneAPI.getSettings().schematicFallbackExtension.value);
        }
        if (!file.exists()) {
            throw new RuntimeException("Could not find schematic: " + schematic + ". Aborting");
        }
        IBuilderProcess builderProcess = BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess();
        builderProcess.build(file.getName(), file, buildOrigin);
        waitUntilTrue(() -> {
            if (builderProcess.isPaused()) {
                resumeIfPausedForFlowingLiquid(builderProcess, schematic, buildOrigin);
            }
            return !builderProcess.isActive();
        });
    }

    private static void resumeIfPausedForFlowingLiquid(IBuilderProcess builderProcess, AutoHighwaySchematic schematic, BlockPos buildOrigin) {
        List<BlockPos> blocks = getSchematicBlockPositions(schematic, buildOrigin);
        boolean containsFlowingWater = Blocks.getBlocknamesAndStatesFromBlockPositions(blocks).stream().anyMatch(b -> b.state().contains(Properties.LEVEL_15) && b.state().get(Properties.LEVEL_15) != 0);
        if (containsFlowingWater) {
            builderProcess.resume();
        }
    }

    private static List<BlockPos> getSchematicBlockPositions(AutoHighwaySchematic schematic, BlockPos buildOrigin) {
        List<BlockPos> blocks = new ArrayList<>();
        switch (schematic) {
            case STEP -> {
                for (int z = -1; z <= 1; z++) {
                    blocks.add(new BlockPos(buildOrigin.getX(), buildOrigin.getY(), z));
                }
            }
            case STEP_UP, STEP_DOWN -> {
                for (int x = 0; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        blocks.add(new BlockPos(buildOrigin.getX() + x, buildOrigin.getY(), z));
                    }
                }
            }
            case STEP_SCAFFOLD -> {
                for (int x = 0; x < 3; x++) {
                    blocks.add(new BlockPos(buildOrigin.getX() + x, buildOrigin.getY(), 0));
                }
            }
            default -> throw new IllegalStateException("Unknown AutoHighwaySchematic in pathing: " + schematic);
        }
        return blocks;
    }


}
