package hein.auto_western_highway.common.building;

import baritone.api.BaritoneAPI;
import baritone.api.Settings;
import hein.auto_western_highway.common.Constants;
import hein.auto_western_highway.common.types.AutoHighwaySchematic;
import hein.auto_western_highway.common.types.StepHeight;
import hein.auto_western_highway.common.utils.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

import static hein.auto_western_highway.common.Globals.globalHudRenderer;
import static hein.auto_western_highway.common.NightLogout.nightLogout;
import static hein.auto_western_highway.common.building.Baritone.build;
import static hein.auto_western_highway.common.building.Baritone.resetSettings;
import static hein.auto_western_highway.common.building.InventoryManagement.replenishItemsIfNeeded;
import static hein.auto_western_highway.common.building.InventoryManagement.setHotbarToInventoryLoadout;
import static hein.auto_western_highway.common.utils.Blocks.*;
import static net.minecraft.util.math.Direction.Axis.*;

public class Up {
    @SuppressWarnings("unused")
    public static StepHeight getStepUpHeight(BlockPos standingBlock) {
        List<String> rayUpBlocks = getBlocksNameFromBlockPositions(getRayUpBlockPositions(standingBlock));
        StepHeight stepHeight = new StepHeight(
                0,
                rayUpBlocks.stream().anyMatch(Blocks::isScaffoldBlockingBlock));
        if (rayUpBlocks.stream().allMatch(Blocks::pathingIgnoreBlocks)) {
            stepHeight.count = 0;
            stepHeight.containsScaffoldBlockingBlocks = false;
            return stepHeight;
        }
        stepHeight.count = 1;
        for (int step = 0; step < Constants.MAX_RAY_STEPS; step++) {
            List<String> blocks = List.of(
                    rayUpBlocks.get(step * 3),
                    rayUpBlocks.get(step * 3 + 1),
                    rayUpBlocks.get(step * 3 + 2)
            );
            if (blocks.stream().allMatch(Blocks::pathingIgnoreBlocks)) {
                stepHeight.count += 1;
            } else {
                break;
            }
        }
        return stepHeight;
    }

    public static List<BlockPos> getRayUpBlockPositions(BlockPos standingBlock) {
        List<BlockPos> blocks = new ArrayList<>(List.of(standingBlock));
        for (int step = 0; step < Constants.MAX_RAY_STEPS; step++) {
            BlockPos stepStartPos = blocks.get(blocks.size() - 1).offset(Y, 1);
            for (int blockStep = 0; blockStep < 3; blockStep++) {
                blocks.add(copyBlock(stepStartPos, -blockStep, 0, 0));
            }
        }
        return blocks.subList(1, blocks.size());
    }

    @SuppressWarnings("unused")
    public static void stepUp(BlockPos buildOrigin, int count) {
        BlockPos tempBuildOrigin = copyBlock(buildOrigin);
        tempBuildOrigin = tempBuildOrigin.offset(Y, 1);
        for (int i = 0; i < count; i++) {
            replenishItemsIfNeeded();
            setHotbarToInventoryLoadout();
            globalHudRenderer.setBuildMessage(String.format("Stepping up %d step%s", count - i, count - i > 1 ? "s" : ""));
            build(AutoHighwaySchematic.STEP_UP, copyBlock(tempBuildOrigin).offset(X, -2).offset(Z, -1));
            tempBuildOrigin = tempBuildOrigin.offset(X, -2).offset(Y, 1);
            nightLogout();
        }
    }

    @SuppressWarnings("unused")
    public static void scaffoldUp(StepHeight stepUpHeight, BlockPos standingBlock) {
        BlockPos buildOrigin = copyBlock(standingBlock);
        buildOrigin = offsetBlock(buildOrigin, 0, -1, 0);
        Settings settings = BaritoneAPI.getSettings();

        settings.buildIgnoreExisting.value = false;
        settings.allowPlace.value = true;
        build(AutoHighwaySchematic.STEP_SCAFFOLD, copyBlock(buildOrigin).offset(X,-3).offset(Y,1));
        settings.allowPlace.value = false;

        settings.buildIgnoreExisting.value = !stepUpHeight.containsScaffoldBlockingBlocks;
        for (int i = 0; i < stepUpHeight.count; i++) {
            replenishItemsIfNeeded();
            setHotbarToInventoryLoadout();
            globalHudRenderer.setBuildMessage(String.format("Scaffolding up %d step%s", stepUpHeight.count - i, stepUpHeight.count - i > 1 ? "s" : ""));
            build(AutoHighwaySchematic.STEP_SCAFFOLD, copyBlock(buildOrigin).offset(X, -3).offset(Y, 1));
            buildOrigin = offsetBlock(buildOrigin, -2, 1, 0);
            nightLogout();
        }
        resetSettings();
    }
}
