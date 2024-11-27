package hein.auto_western_highway.common.building;

import hein.auto_western_highway.common.Constants;
import hein.auto_western_highway.common.types.StepFunctionWithCount;
import hein.auto_western_highway.common.types.StepHeight;
import net.minecraft.util.math.BlockPos;

import java.util.List;

import static hein.auto_western_highway.common.building.Movement.adjustStandingBlock;
import static hein.auto_western_highway.common.utils.Blocks.*;
import static hein.auto_western_highway.common.utils.Reflections.getMethod;

public class DetermineStepFunction {
    public static List<StepFunctionWithCount> determineStepFunction(BlockPos position) {
        List<StepFunctionWithCount> nextMoves;
        nextMoves = tryStepUpDown("Up", position);
        if (nextMoves != null) {
            return nextMoves;
        }
        nextMoves = tryStepUpDown("Down", position);
        if (nextMoves != null) {
            return nextMoves;
        }
        nextMoves = List.of(new StepFunctionWithCount(getMethod("common.building.Step", "step", BlockPos.class, int.class), new StepHeight(1)));
        return nextMoves;
    }

    private static List<StepFunctionWithCount> tryStepUpDown(String direction, BlockPos position) {
        if (!direction.equals("Up") && !direction.equals("Down")) {
            return null;
        }
        StepHeight stepHeight;
        try {
            stepHeight = (StepHeight) getMethod("common.building." + direction, String.format("getStep%sHeight", direction), position.getClass()).invoke(null, position);
        } catch (Exception e) {
            return null;
        }
        if (stepHeight.count == 0) {
            return null;
        }
        BlockPos futurePosition = copyBlock(position);
        futurePosition = adjustStandingBlock(futurePosition, new StepFunctionWithCount(getMethod("common.building." + direction, String.format("step%s", direction), BlockPos.class, int.class), stepHeight));
        StepHeight reverseStepHeight = new StepHeight(0);
        try {
            for (int i = 0; i < Constants.FUTURE_STEPS; i++) {
                futurePosition = offsetBlock(futurePosition, -i, 0, 0);
                if (!getBlocksNameFromBlockPositions(List.of(futurePosition)).contains("air")) {
                    break;
                }
                StepHeight tempReverseStepHeight = (StepHeight) getMethod("common.building." + reverseDirection(direction), String.format("getStep%sHeight", reverseDirection(direction)), futurePosition.getClass()).invoke(null, futurePosition);
                if (tempReverseStepHeight.count > reverseStepHeight.count) {
                    reverseStepHeight = tempReverseStepHeight;
                }
            }
        } catch (Exception e) {
            return null;
        }
        int diff = stepHeight.count - reverseStepHeight.count;

        if (diff <= 0) {
            return null;
        }
        return List.of(
                new StepFunctionWithCount(
                        getMethod("common.building." + direction, String.format("step%s", direction), BlockPos.class, int.class),
                        getMethod("common.building." + direction, String.format("scaffold%s", direction), StepHeight.class, BlockPos.class),
                        new StepHeight(diff, stepHeight.containsScaffoldBlockingBlocks)
                ),
                new StepFunctionWithCount(
                        getMethod("common.building.Step", "step", BlockPos.class, int.class),
                        new StepHeight(
                                reverseStepHeight.count * 2 * 2, // 2 steps forward per step up/down, doubled because of the immediate up <-> down
                                stepHeight.containsScaffoldBlockingBlocks
                        )
                )
        );
    }

    private static String reverseDirection(String direction) {
        if (direction.equals("Down")) {
            return "Up";
        }
        if (direction.equals("Up")) {
            return "Down";
        }
        return null;
    }
}
