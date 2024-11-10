package hein.auto_western_highway;

public enum AutoHighwaySchematic {
    STEP("step"),
    STEP_UP("step_up"),
    STEP_DOWN("step_down"),
    STEP_SCAFFOLD("step_scaffold");

    private final String fileName;

    AutoHighwaySchematic(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return this.fileName;
    }
}
