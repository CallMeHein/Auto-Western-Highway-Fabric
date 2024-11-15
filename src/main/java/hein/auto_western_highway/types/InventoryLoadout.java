package hein.auto_western_highway.types;

import java.util.List;

public class InventoryLoadout {
    // The loadout we force on the user
    public static final List<ResourceLoadout> inventoryLoadout = List.of(
            new ResourceLoadout("stone_bricks", 40, 129, 4),
            new ResourceLoadout("stone_brick_slab", 40, 65, 5),
            new ResourceLoadout("smooth_stone", 40, 65, 6),
            new ResourceLoadout("smooth_stone_slab", 40, 65, 7),
            new ResourceLoadout("dirt", 40, 65, 8)
    );

    public static void resetInventoryLoadout() {
        inventoryLoadout.forEach(b -> b.count = 0);
    }

    public static void addToInventoryLoadout(String blockId, int amount) {
        inventoryLoadout.stream().filter(b -> b.block.equals(blockId)).toList().get(0).count += amount;
    }

    public static List<String> getInventoryLoadoutBlocks() {
        return inventoryLoadout.stream().map(b -> b.block).toList();
    }

    public static List<ResourceLoadout> getLowMaterials() {
        return inventoryLoadout.stream().filter(b -> b.count <= b.minimumCount).toList();
    }

    public static List<ResourceLoadout> getUnfilledMaterials() {
        return inventoryLoadout.stream().filter(b -> b.count < b.fillCount).toList();
    }
}
