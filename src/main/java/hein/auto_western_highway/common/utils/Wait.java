package hein.auto_western_highway.common.utils;

import java.util.function.Supplier;

public class Wait {
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

    public static boolean waitUntilTrueWithTimeout(Supplier<Boolean> condition, int pollingRateMs, int timeoutMs) {
        int repeatCount = timeoutMs / pollingRateMs; // assume devs set timeouts that are actually multiples of the polling rate
        for (int i = 0; i < repeatCount; i++) {
            sleep(pollingRateMs);
            if (condition.get()) {
                return true;
            }
        }
        return false;
    }
}
