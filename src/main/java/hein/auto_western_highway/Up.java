package hein.auto_western_highway;

import baritone.api.BaritoneAPI;
import baritone.api.Settings;
import hein.auto_western_highway.types.StepHeight;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.List;

import static hein.auto_western_highway.Baritone.build;
import static hein.auto_western_highway.Baritone.resetSettings;
import static hein.auto_western_highway.Blocks.copyBlock;
import static hein.auto_western_highway.Blocks.getBlocksNameFromBlockPos;
import static net.minecraft.util.math.Direction.Axis.*;

public class Up {
    public static StepHeight getStepUpHeight(ClientPlayerEntity player, BlockPos standingBlock) {
        List<String> rayUpBlocks = getBlocksNameFromBlockPos(player, getRayUpBlockPositions(standingBlock));
        StepHeight stepHeight = new StepHeight();
        stepHeight.containsScaffoldBlockingBlocks = rayUpBlocks.stream().anyMatch(Blocks::isScaffoldBlockingBlock);
        if (rayUpBlocks.stream().allMatch(Blocks::isNonTerrainBlock)) {
            stepHeight.height = 0;
            stepHeight.containsScaffoldBlockingBlocks = false;
            return stepHeight;
        }
        stepHeight.height = 1;
        for (int step = 0; step < Constants.MAX_RAY_STEPS; step++) {
            List<String> blocks = List.of(
                    rayUpBlocks.get(step * 3),
                    rayUpBlocks.get(step * 3 + 1),
                    rayUpBlocks.get(step * 3 + 2)
            );
            if (blocks.stream().allMatch(Blocks::isNonTerrainBlock)) {
                stepHeight.height += 1;
            } else {
                break;
            }
        }
        return stepHeight;
    }

    private static List<BlockPos> getRayUpBlockPositions(BlockPos standingBlock) {
        List<BlockPos> blocks = new ArrayList<>(List.of(standingBlock));
        for (int step = 0; step < Constants.MAX_RAY_STEPS; step++) {
            BlockPos stepStartPos = blocks.get(blocks.size() - 1).offset(Y, 1);
            for (int blockStep = 0; blockStep < 3; blockStep++) {
                blocks.add(copyBlock(stepStartPos, -blockStep, 0, 0));
            }
        }
        return blocks.subList(1, blocks.size());
    }

    public static BlockPos stepUp(int count, BlockPos buildOrigin) {
        buildOrigin = buildOrigin.offset(Y, 1);
        for (int i = 0; i < count; i++) {
            build("step_up", copyBlock(buildOrigin).offset(X, -2).offset(Z, -1));
            buildOrigin = buildOrigin.offset(X, -2).offset(Y, 1);
        }
        return buildOrigin.offset(Y, -1);
    }

    public static void upwardScaffold(StepHeight stepUpHeight, BlockPos buildOrigin) {
        Settings settings = BaritoneAPI.getSettings();
        settings.buildIgnoreExisting.value = !stepUpHeight.containsScaffoldBlockingBlocks;
        settings.buildRepeat.value = new Vec3i(-2, 1, 0);
        settings.buildRepeatCount.value = stepUpHeight.height;
        build("step_scaffold", copyBlock(buildOrigin).offset(X, -3).offset(Y, 1));
        resetSettings();
    }

    public static int getFutureStepUpLength(ClientPlayerEntity player, BlockPos standingBlock, int stepDownHeight) {
        BlockPos stepUpBlock = copyBlock(standingBlock).offset(X, -2 * stepDownHeight).offset(Y, -stepDownHeight);
        for (int futureStep = 0; futureStep < Constants.FUTURE_STEPS; futureStep++) {
            BlockPos futureBlock = stepUpBlock.offset(X, -futureStep);
            StepHeight futureStepDownHeight = getStepUpHeight(player, futureBlock);
            if (futureStepDownHeight.height >= stepDownHeight) {
                return 4 * stepDownHeight + futureStep;
            }
        }
        return 0;
    }
}
