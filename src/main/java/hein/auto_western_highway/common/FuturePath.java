package hein.auto_western_highway.common;

import baritone.api.BaritoneAPI;
import hein.auto_western_highway.common.types.StepFunctionWithCount;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

import static hein.auto_western_highway.common.AutoWesternHighway.displayFuturePath;
import static hein.auto_western_highway.common.AutoWesternHighway.running;
import static hein.auto_western_highway.common.BlockRenderer.blockRendererBlocks;
import static hein.auto_western_highway.common.Down.getRayDownBlockPositions;
import static hein.auto_western_highway.common.Movement.adjustStandingBlock;
import static hein.auto_western_highway.common.Movement.getStepFunction;
import static hein.auto_western_highway.common.Up.getRayUpBlockPositions;
import static hein.auto_western_highway.common.Utils.getStandingBlock;
import static hein.auto_western_highway.common.Utils.sleep;

public class FuturePath {
    private static BlockPos lastProcessedHighwayPosition;

    public static void renderFuturePath() throws NoSuchMethodException {
        while (true) {
            if (!running || !displayFuturePath || BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess().isPaused()) {
                sleep(2000);
                continue;
            }
            BlockPos currentStandingBlock = getStandingBlock();
            BlockPos currentHighwayPosition = new BlockPos(currentStandingBlock.getX(), currentStandingBlock.getY(), 0);
            if (currentHighwayPosition.equals(lastProcessedHighwayPosition)) {
                sleep(2000);
                continue;
            }
            BlockPos currentFuturePathPosition = currentHighwayPosition;
            List<BlockPos> futurePathBlocks = new ArrayList<>();
            // runs until it hits the end of the render distance, breaks out of the loop through the index error
            while (true) {
                try {
                    StepFunctionWithCount stepFunction = getStepFunction(currentFuturePathPosition);
                    assert stepFunction != null;
                    if (stepFunction.stepFunction.equals(Step.class.getMethod("step", BlockPos.class, int.class))) {
                        for (int i = 1; i <= stepFunction.stepHeight.count; i++) {
                            futurePathBlocks.add(
                                    new BlockPos(
                                            currentFuturePathPosition.getX() - i,
                                            currentFuturePathPosition.getY(),
                                            currentFuturePathPosition.getZ()
                                    )
                            );
                        }
                    } else if (stepFunction.stepFunction.equals(Up.class.getMethod("stepUp", BlockPos.class, int.class))) {
                        List<BlockPos> stepUpBlockPosition = getRayUpBlockPositions(currentFuturePathPosition).subList(0, stepFunction.stepHeight.count * 3 + 1);
                        futurePathBlocks.addAll(stepUpBlockPosition);
                    } else if (stepFunction.stepFunction.equals(Down.class.getMethod("stepDown", BlockPos.class, int.class))) {
                        List<BlockPos> stepDownBlockPosition = getRayDownBlockPositions(currentFuturePathPosition);
                        stepDownBlockPosition = stepDownBlockPosition.subList(0, stepFunction.stepHeight.count * 3 + 1);
                        futurePathBlocks.addAll(stepDownBlockPosition);
                    } else {
                        throw new IllegalStateException("Unhandled StepFunction: " + stepFunction.stepFunction);
                    }
                    currentFuturePathPosition = adjustStandingBlock(currentFuturePathPosition, stepFunction);
                } catch (IndexOutOfBoundsException e) {
                    break;
                }
            }
            blockRendererBlocks = futurePathBlocks;
            lastProcessedHighwayPosition = currentHighwayPosition;
        }
    }
}
