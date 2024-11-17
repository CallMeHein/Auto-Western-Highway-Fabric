package hein.auto_western_highway.common.types;

import java.lang.reflect.Method;

public class StepFunctionWithCount {
    public final StepHeight stepHeight;
    public final Method stepFunction;
    public Method scaffoldFunction;

    public StepFunctionWithCount(Method stepFunction, StepHeight stepHeight) {
        this.stepFunction = stepFunction;
        this.stepHeight = stepHeight;
    }

    public StepFunctionWithCount(Method stepFunction, Method scaffoldFunction, StepHeight stepHeight) {
        this.stepFunction = stepFunction;
        this.scaffoldFunction = scaffoldFunction;
        this.stepHeight = stepHeight;
    }
}
