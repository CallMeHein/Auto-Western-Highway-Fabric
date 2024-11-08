package hein.auto_western_highway;

import baritone.api.BaritoneAPI;
import baritone.api.Settings;
import baritone.api.process.IBuilderProcess;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.List;

import static hein.auto_western_highway.Constants.buildIgnoreBlocks;
import static hein.auto_western_highway.Utils.sleep;
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

    public static void build(String schematic, BlockPos coordinates) {
        File file = new File(new File(BaritoneAPI.getProvider().getPrimaryBaritone().getPlayerContext().minecraft().runDirectory, "schematics"), schematic + ".litematic").getAbsoluteFile();
        if (FilenameUtils.getExtension(file.getAbsolutePath()).isEmpty()) {
            file = new File(file.getAbsolutePath() + "." + BaritoneAPI.getSettings().schematicFallbackExtension.value);
        }
        IBuilderProcess builderProcess = BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess();
        builderProcess.build(file.getName(), file, coordinates);
        waitUntilDone(builderProcess);
    }

    private static void waitUntilDone(IBuilderProcess builderProcess) {
        while (builderProcess.isActive()) {
            sleep(250);
        }
    }
}
