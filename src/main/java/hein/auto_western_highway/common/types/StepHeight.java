package hein.auto_western_highway.common.types;

public class StepHeight {
    public int count;
    public boolean containsScaffoldBlockingBlocks;

    public StepHeight(int height) {
        this.count = height;
    }

    public StepHeight(int height, boolean containsScaffoldBlockingBlocks) {
        this.count = height;
        this.containsScaffoldBlockingBlocks = containsScaffoldBlockingBlocks;
    }
}
