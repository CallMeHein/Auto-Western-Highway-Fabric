package hein.auto_western_highway;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;

import static hein.auto_western_highway.AutoHighwaySchematic.STEP;
import static net.minecraft.util.math.Direction.Axis.X;
import static net.minecraft.util.math.Direction.Axis.Z;

public class Step {
    public static BlockPos step(ClientPlayerEntity player, BlockPos buildOrigin, int count) {
        for (int i = 0; i < count; i++) {
            Baritone.build(player, STEP, buildOrigin.offset(X, -1).offset(Z, -1));
            buildOrigin = buildOrigin.offset(X, -1);
        }
        return buildOrigin;
    }
}
