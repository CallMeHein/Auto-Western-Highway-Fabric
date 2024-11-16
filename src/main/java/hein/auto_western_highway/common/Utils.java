package hein.auto_western_highway.common;

import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.function.Supplier;

import static hein.auto_western_highway.common.Blocks.offsetBlock;
import static hein.auto_western_highway.common.Globals.globalPlayer;

public class Utils {
    public static void sendStatusMessage(String message) {
        globalPlayer.sendMessage(Text.literal(message));
    }

    public static BlockPos getStandingBlock() {
        return offsetBlock(getPlayerFeetBlock(), 0, -1, 0);
    }

    public static BlockPos getPlayerFeetBlock() {
        return globalPlayer.getBlockPos();
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
