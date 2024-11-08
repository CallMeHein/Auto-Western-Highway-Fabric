package hein.auto_western_highway;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import static hein.auto_western_highway.Blocks.offsetBlock;

public class Utils {
    public static void sendStatusMessage(ClientPlayerEntity player, String message) {
        player.sendMessage(Text.literal(message));
    }

    public static BlockPos getStandingBlock(ClientPlayerEntity player) {
        return offsetBlock(getPlayerFeetBlock(player), 0, -1, 0);
    }

    public static BlockPos getPlayerFeetBlock(ClientPlayerEntity player) {
        return player.getBlockPos();
    }


    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}
