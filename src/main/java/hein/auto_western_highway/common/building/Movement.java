package hein.auto_western_highway.common.building;

import hein.auto_western_highway.common.types.StepFunctionWithCount;
import hein.auto_western_highway.common.types.StepHeight;
import net.minecraft.util.math.BlockPos;

import static hein.auto_western_highway.common.building.Down.getFutureStepDownLength;
import static hein.auto_western_highway.common.building.Down.getStepDownHeight;
import static hein.auto_western_highway.common.building.Up.getFutureStepUpLength;
import static hein.auto_western_highway.common.building.Up.getStepUpHeight;
import static hein.auto_western_highway.common.utils.Blocks.offsetBlock;
import static hein.auto_western_highway.common.utils.Reflections.getMethod;

public class Movement {
    public static BlockPos adjustStandingBlock(BlockPos standingBlock, StepFunctionWithCount stepFunction) {
        try {
            if (stepFunction.stepFunction.equals(getMethod("common.building.Step", "step", BlockPos.class, int.class))) {
                return offsetBlock(standingBlock, -stepFunction.stepHeight.count, 0, 0);
            }
            if (stepFunction.stepFunction.equals(getMethod("common.building.Up", "stepUp", BlockPos.class, int.class))) {
                return offsetBlock(standingBlock, -2 * stepFunction.stepHeight.count, stepFunction.stepHeight.count, 0);
            }
            if (stepFunction.stepFunction.equals(getMethod("common.building.Down", "stepDown", BlockPos.class, int.class))) {
                return offsetBlock(standingBlock, -2 * stepFunction.stepHeight.count, -stepFunction.stepHeight.count, 0);
            }
            throw new IllegalStateException("Unhandled StepFunction: " + stepFunction.stepFunction);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static StepFunctionWithCount getStepFunction(BlockPos standingBlock) {
        try {
            // check if/how much we should step up
            StepHeight stepUpHeight = getStepUpHeight(standingBlock);
            if (stepUpHeight.count > 0) {
                int futureStepDownLength = getFutureStepDownLength(standingBlock, stepUpHeight.count);
                if (futureStepDownLength > 0) {
                    return new StepFunctionWithCount(getMethod("common.building.Step", "step", BlockPos.class, int.class), new StepHeight(futureStepDownLength));
                }
                return new StepFunctionWithCount(
                        getMethod("common.building.Up", "stepUp", BlockPos.class, int.class),
                        getMethod("common.building.Up", "scaffoldUp", StepHeight.class, BlockPos.class),
                        stepUpHeight);
            }
            // check if/how much we should step down
            StepHeight stepDownHeight = getStepDownHeight(standingBlock);
            if (stepDownHeight.count > 0) {
                int futureStepUpLength = getFutureStepUpLength(standingBlock, stepUpHeight.count);
                if (futureStepUpLength > 0) {
                    return new StepFunctionWithCount(getMethod("common.building.Step", "step", BlockPos.class, int.class), new StepHeight(futureStepUpLength));
                }
                return new StepFunctionWithCount(
                        getMethod("common.building.Down", "stepDown", BlockPos.class, int.class),
                        getMethod("common.building.Down", "scaffoldDown", StepHeight.class, BlockPos.class),
                        stepDownHeight);
            }
            // otherwise just take one step forward
            return new StepFunctionWithCount(getMethod("common.building.Step", "step", BlockPos.class, int.class), new StepHeight(1));
        } catch (Exception e) {
            return null;
        }
    }
}
