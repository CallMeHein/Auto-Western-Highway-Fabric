package hein.auto_western_highway.common.building;

import hein.auto_western_highway.common.types.StepFunctionWithCount;
import net.minecraft.util.math.BlockPos;

import java.lang.reflect.InvocationTargetException;

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
}
