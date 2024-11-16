package hein.auto_western_highway.common;

import net.minecraft.util.math.BlockPos;

import static hein.auto_western_highway.common.AutoHighwaySchematic.STEP;
import static hein.auto_western_highway.common.Globals.globalHudRenderer;
import static hein.auto_western_highway.common.InventoryManagement.replenishItemsIfNeeded;
import static hein.auto_western_highway.common.InventoryManagement.setHotbarToInventoryLoadout;
import static net.minecraft.util.math.Direction.Axis.X;
import static net.minecraft.util.math.Direction.Axis.Z;

public class Step {
    public static BlockPos step(BlockPos buildOrigin, int count) {
        for (int i = 0; i < count; i++) {
            replenishItemsIfNeeded();
            setHotbarToInventoryLoadout();
            globalHudRenderer.message = String.format("Stepping %d block%s", count - i, count - i > 1 ? "s" : "");
            Baritone.build(STEP, buildOrigin.offset(X, -1).offset(Z, -1));
            buildOrigin = buildOrigin.offset(X, -1);
        }
        return buildOrigin;
    }
}
