package hein.auto_western_highway.common;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

import static hein.auto_western_highway.common.Globals.globalPlayer;


public class BlockRenderer {
    public static List<BlockPos> blockRendererBlocks;

    public static void renderBlocks(WorldRenderContext worldRenderContext) {
        if (globalPlayer.get() == null || blockRendererBlocks == null || blockRendererBlocks.isEmpty()) {
            return;
        }
        MatrixStack matrices = worldRenderContext.matrixStack();
        VertexConsumer vertexConsumer = worldRenderContext.consumers().getBuffer(RenderLayer.getLines());
        Vec3d cameraPos = worldRenderContext.camera().getPos();

        matrices.push();
        try {
            blockRendererBlocks.forEach(blockPos -> {
                double x = blockPos.getX() - cameraPos.x;
                double y = blockPos.getY() - cameraPos.y;
                double z = blockPos.getZ() - cameraPos.z;

                Box box = new Box(x, y, z, x + 1, y + 1, z + 1);
                WorldRenderer.drawBox(matrices, vertexConsumer, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, 0.66f, 0.66f, 0.66f, 1);
            });
        } finally {
            matrices.pop();
        }
    }
}
