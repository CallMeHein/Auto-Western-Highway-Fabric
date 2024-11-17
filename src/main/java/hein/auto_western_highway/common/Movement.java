package hein.auto_western_highway.common;

import hein.auto_western_highway.common.types.StepFunctionWithCount;
import hein.auto_western_highway.common.types.StepHeight;
import net.minecraft.util.math.BlockPos;

import static hein.auto_western_highway.common.Blocks.offsetBlock;
import static hein.auto_western_highway.common.Down.getFutureStepDownLength;
import static hein.auto_western_highway.common.Down.getStepDownHeight;
import static hein.auto_western_highway.common.Up.getFutureStepUpLength;
import static hein.auto_western_highway.common.Up.getStepUpHeight;

public class Movement {
    public static BlockPos adjustStandingBlock(BlockPos standingBlock, StepFunctionWithCount stepFunction) {
        try {
            if (stepFunction.stepFunction.equals(Step.class.getMethod("step", BlockPos.class, int.class))) {
                return offsetBlock(standingBlock, -stepFunction.stepHeight.count, 0, 0);
            }
            if (stepFunction.stepFunction.equals(Up.class.getMethod("stepUp", BlockPos.class, int.class))) {
                return offsetBlock(standingBlock, -2 * stepFunction.stepHeight.count, stepFunction.stepHeight.count, 0);
            }
            if (stepFunction.stepFunction.equals(Down.class.getMethod("stepDown", BlockPos.class, int.class))) {
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
                    return new StepFunctionWithCount(Step.class.getMethod("step", BlockPos.class, int.class), new StepHeight(futureStepDownLength));
                }
                return new StepFunctionWithCount(
                        Up.class.getMethod("stepUp", BlockPos.class, int.class),
                        Up.class.getMethod("scaffoldUp", StepHeight.class, BlockPos.class),
                        stepUpHeight);
            }
            // check if/how much we should step down
            StepHeight stepDownHeight = getStepDownHeight(standingBlock);
            if (stepDownHeight.count > 0) {
                int futureStepUpLength = getFutureStepUpLength(standingBlock, stepUpHeight.count);
                if (futureStepUpLength > 0) {
                    return new StepFunctionWithCount(Step.class.getMethod("step", BlockPos.class, int.class), new StepHeight(futureStepUpLength));
                }
                return new StepFunctionWithCount(
                        Down.class.getMethod("stepDown", BlockPos.class, int.class),
                        Down.class.getMethod("scaffoldDown", StepHeight.class, BlockPos.class),
                        stepDownHeight);
            }
            // otherwise just take one step forward
            return new StepFunctionWithCount(Step.class.getMethod("step", BlockPos.class, int.class), new StepHeight(1));
        } catch (Exception e) {
            return null;
        }
    }
}
