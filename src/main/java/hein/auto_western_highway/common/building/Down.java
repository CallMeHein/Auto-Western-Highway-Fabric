package hein.auto_western_highway.common.building;

import baritone.api.BaritoneAPI;
import baritone.api.Settings;
import hein.auto_western_highway.common.Constants;
import hein.auto_western_highway.common.types.StepHeight;
import hein.auto_western_highway.common.utils.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

import static hein.auto_western_highway.common.Globals.globalHudRenderer;
import static hein.auto_western_highway.common.building.Baritone.*;
import static hein.auto_western_highway.common.building.InventoryManagement.replenishItemsIfNeeded;
import static hein.auto_western_highway.common.building.InventoryManagement.setHotbarToInventoryLoadout;
import static hein.auto_western_highway.common.types.AutoHighwaySchematic.*;
import static hein.auto_western_highway.common.utils.Blocks.*;
import static net.minecraft.util.math.Direction.Axis.Y;

public class Down {
    @SuppressWarnings("unused")
    public static StepHeight getStepDownHeight(BlockPos standingBlock) {
        List<String> rayDownBlocks = getBlocksNameFromBlockPositions(getRayDownBlockPositions(standingBlock));
        StepHeight stepHeight = new StepHeight(
                0,
                rayDownBlocks.stream().anyMatch(Blocks::isScaffoldBlockingBlock)
        );
        for (int step = 0; step < Constants.MAX_RAY_STEPS; step++) {
            List<String> blocks = List.of(
                    rayDownBlocks.get(step * 3),
                    rayDownBlocks.get(step * 3 + 1),
                    rayDownBlocks.get(step * 3 + 2)
            );
            // on the 0-th step, ignore the block we are standing on
            if (step == 0 && pathingIgnoreBlocks(blocks.get(1)) && pathingIgnoreBlocks(blocks.get(2))) {
                stepHeight.count += 1;
            } else if (blocks.stream().allMatch(Blocks::pathingIgnoreBlocks)) {
                stepHeight.count += 1;
            } else {
                break;
            }
        }
        return stepHeight;
    }

    public static List<BlockPos> getRayDownBlockPositions(BlockPos standingBlock) {
        List<BlockPos> blocks = new ArrayList<>(List.of(copyBlock(standingBlock, 0, 1, 0)));
        for (int step = 0; step < Constants.MAX_RAY_STEPS; step++) {
            BlockPos stepStartPos = offsetBlock(blocks.get(blocks.size() - 1), 0, -1, 0);
            for (int blockStep = 0; blockStep < 3; blockStep++) {
                blocks.add(copyBlock(stepStartPos, -blockStep, 0, 0));
            }
        }
        return blocks.subList(1, blocks.size());
    }

    @SuppressWarnings("unused")
    public static void stepDown(BlockPos buildOrigin, int count) {
        BlockPos tempBuildOrigin = copyBlock(buildOrigin);
        tempBuildOrigin = tempBuildOrigin.offset(Y, 1);
        for (int i = 0; i < count; i++) {
            replenishItemsIfNeeded();
            setHotbarToInventoryLoadout();
            globalHudRenderer.buildMessage = String.format("Stepping down %d step%s", count - i, count - i > 1 ? "s" : "");
            build(STEP_DOWN, copyBlock(tempBuildOrigin, -1, -1, -1));
            build(STEP, copyBlock(tempBuildOrigin, -2, -2, -1));
            tempBuildOrigin = offsetBlock(tempBuildOrigin, -2, -1, 0);
        }
    }

    @SuppressWarnings("unused")
    public static void scaffoldDown(StepHeight stepDownHeight, BlockPos standingBlock) {
        BlockPos buildOrigin = copyBlock(standingBlock);
        buildOrigin = offsetBlock(buildOrigin, 0, 1, 0);
        Settings settings = BaritoneAPI.getSettings();

        BlockPos preScaffoldBuildOrigin = copyBlock(buildOrigin, -2, -(stepDownHeight.count + 1), 0);
        for (int i = 0; i < stepDownHeight.count; i++) {
            replenishItemsIfNeeded();
            setHotbarToInventoryLoadout();
            globalHudRenderer.buildMessage = String.format("Pre-scaffolding down %d step%s", stepDownHeight.count - i, stepDownHeight.count - i > 1 ? "s" : "");
            List<String> blocks = Blocks.getBlocksNameFromBlockPositions(getSchematicBlockPositions(STEP_SCAFFOLD, preScaffoldBuildOrigin));
            settings.buildIgnoreExisting.value = !blocks.contains("water");
            build(STEP_SCAFFOLD, copyBlock(preScaffoldBuildOrigin));
            preScaffoldBuildOrigin = offsetBlock(preScaffoldBuildOrigin, -2, 0, 0);
        }
        settings.buildIgnoreExisting.value = !stepDownHeight.containsScaffoldBlockingBlocks;
        int scaffoldSteps = stepDownHeight.count + 1;
        for (int i = 0; i < scaffoldSteps; i++) {
            replenishItemsIfNeeded();
            setHotbarToInventoryLoadout();
            globalHudRenderer.buildMessage = String.format("Scaffolding down %d step%s", scaffoldSteps - i, scaffoldSteps - i > 1 ? "s" : "");
            build(STEP_SCAFFOLD, copyBlock(buildOrigin, -2 * scaffoldSteps, -scaffoldSteps, 0));
            buildOrigin = offsetBlock(buildOrigin, 2, 1, 0);
        }
        resetSettings();
    }
}
