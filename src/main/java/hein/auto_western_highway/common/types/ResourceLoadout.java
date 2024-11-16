package hein.auto_western_highway.common.types;

public class ResourceLoadout {
    public final String block;
    public final int minimumCount;
    public final int fillCount;
    public final int preferredSlotId;
    public int count;

    public ResourceLoadout(String block, int minimumCount, int fillCount, int preferredSlotId) {
        this.block = block;
        this.count = 0;
        this.minimumCount = minimumCount;
        this.fillCount = fillCount;
        this.preferredSlotId = preferredSlotId;
    }
}
