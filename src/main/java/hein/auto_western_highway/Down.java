package hein.auto_western_highway;

import baritone.api.BaritoneAPI;
import baritone.api.Settings;
import hein.auto_western_highway.types.StepHeight;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

import static hein.auto_western_highway.AutoHighwaySchematic.*;
import static hein.auto_western_highway.Baritone.build;
import static hein.auto_western_highway.Baritone.resetSettings;
import static hein.auto_western_highway.Blocks.*;
import static net.minecraft.util.math.Direction.Axis.X;
import static net.minecraft.util.math.Direction.Axis.Y;

public class Down {
    public static StepHeight getStepDownHeight(ClientPlayerEntity player, BlockPos standingBlock) {
        List<String> rayDownBlocks = getBlocksNameFromBlockPositions(player, getRayDownBlockPositions(standingBlock));
        StepHeight stepHeight = new StepHeight();
        stepHeight.containsScaffoldBlockingBlocks = rayDownBlocks.stream().anyMatch(Blocks::isScaffoldBlockingBlock);
        stepHeight.height = 0;
        for (int step = 0; step < Constants.MAX_RAY_STEPS; step++) //noinspection GrazieInspection
        {
            List<String> blocks = List.of(
                    rayDownBlocks.get(step * 3),
                    rayDownBlocks.get(step * 3 + 1),
                    rayDownBlocks.get(step * 3 + 2)
            );
            // ignore the block we are standing on on the 0-th step
            if (step == 0 && isNonTerrainBlock(blocks.get(1)) && isNonTerrainBlock(blocks.get(2))) {
                stepHeight.height += 1;
            } else if (blocks.stream().allMatch(Blocks::isNonTerrainBlock)) {
                stepHeight.height += 1;
            } else {
                break;
            }
        }
        return stepHeight;
    }

    private static List<BlockPos> getRayDownBlockPositions(BlockPos standingBlock) {
        List<BlockPos> blocks = new ArrayList<>(List.of(copyBlock(standingBlock, 0, 1, 0)));
        for (int step = 0; step < Constants.MAX_RAY_STEPS; step++) {
            BlockPos stepStartPos = offsetBlock(blocks.get(blocks.size() - 1), 0, -1, 0);
            for (int blockStep = 0; blockStep < 3; blockStep++) {
                blocks.add(copyBlock(stepStartPos, -blockStep, 0, 0));
            }
        }
        return blocks.subList(1, blocks.size());
    }

    public static BlockPos stepDown(ClientPlayerEntity player, int count, BlockPos buildOrigin) {
        buildOrigin = buildOrigin.offset(Y, 1);
        for (int i = 0; i < count; i++) {
            build(player, STEP_DOWN, copyBlock(buildOrigin, -1, -1, -1));
            build(player, STEP, copyBlock(buildOrigin, -2, -2, -1));
            buildOrigin = offsetBlock(buildOrigin, -2, -1, 0);
        }
        return buildOrigin.offset(Y, -1);
    }

    public static void downwardScaffold(ClientPlayerEntity player, StepHeight stepDownHeight, BlockPos standingBlock) {
        BlockPos buildOrigin = copyBlock(standingBlock);
        Settings settings = BaritoneAPI.getSettings();
        settings.buildIgnoreExisting.value = !stepDownHeight.containsScaffoldBlockingBlocks;
        for (int i = 0; i < stepDownHeight.height; i++) {
            build(player, STEP_SCAFFOLD, copyBlock(buildOrigin, -2 * stepDownHeight.height, -stepDownHeight.height, 0));
            buildOrigin = offsetBlock(buildOrigin, 2, 1, 0);
        }
        resetSettings();
    }

    public static int getFutureStepDownLength(ClientPlayerEntity player, BlockPos standingBlock, int stepUpHeight) {
        BlockPos stepUpBlock = copyBlock(standingBlock, -2 * stepUpHeight, stepUpHeight, 0);
        for (int futureStep = 0; futureStep < Constants.FUTURE_STEPS; futureStep++) {
            BlockPos futureBlock = stepUpBlock.offset(X, -futureStep);
            StepHeight futureStepDownHeight = getStepDownHeight(player, futureBlock);
            if (futureStepDownHeight.height >= stepUpHeight) {
                return 4 * stepUpHeight + futureStep;
            }
        }
        return 0;
    }

}
