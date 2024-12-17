package hein.auto_western_highway.common.building;

import net.minecraft.util.math.BlockPos;

import static hein.auto_western_highway.common.Globals.globalHudRenderer;
import static hein.auto_western_highway.common.building.InventoryManagement.replenishItemsIfNeeded;
import static hein.auto_western_highway.common.building.InventoryManagement.setHotbarToInventoryLoadout;
import static hein.auto_western_highway.common.types.AutoHighwaySchematic.STEP;
import static hein.auto_western_highway.common.utils.Blocks.copyBlock;
import static net.minecraft.util.math.Direction.Axis.X;
import static net.minecraft.util.math.Direction.Axis.Z;

@SuppressWarnings("unused")
public class Step {
    @SuppressWarnings("unused")
    public static void step(BlockPos buildOrigin, int count) {
        BlockPos tempBuildOrigin = copyBlock(buildOrigin);
        for (int i = 0; i < count; i++) {
            replenishItemsIfNeeded();
            setHotbarToInventoryLoadout();
            globalHudRenderer.setBuildMessage(String.format("Stepping %d block%s", count - i, count - i > 1 ? "s" : ""));
            Baritone.build(STEP, tempBuildOrigin.offset(X, -1).offset(Z, -1));
            tempBuildOrigin = tempBuildOrigin.offset(X, -1);
        }
    }
}
