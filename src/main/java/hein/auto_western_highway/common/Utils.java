package hein.auto_western_highway.common;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.function.Supplier;

import static hein.auto_western_highway.common.Blocks.offsetBlock;
import static hein.auto_western_highway.common.Globals.globalPlayer;

public class Utils {
    public static void sendStatusMessage(String message) {
        ClientPlayerEntity player = globalPlayer.get();
        if (player != null) {
            player.sendMessage(Text.literal(message));
        }
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

    public static void waitUntilTrue(Supplier<Boolean> condition, int pollingRateMs) {
        while (!condition.get()) {
            sleep(pollingRateMs);
        }
    }

    public static void waitUntilTrue(Supplier<Boolean> condition) {
        waitUntilTrue(condition, 200);
    }


}
