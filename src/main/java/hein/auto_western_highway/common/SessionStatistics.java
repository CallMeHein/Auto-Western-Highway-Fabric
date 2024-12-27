package hein.auto_western_highway.common;

import java.time.Duration;
import java.time.Instant;

import static hein.auto_western_highway.common.Globals.*;
import static java.lang.Math.abs;

public class SessionStatistics {
    public static long startTime = 0;
    public static long endTime = 0;
    public static int startX = 0;
    public static int endX = 0;

    public static void startSessionStatistics(){
        startTime = Instant.now().getEpochSecond();
        startX = globalPlayerNonNull.get().getBlockPos().getX();
    }

    public static void logSessionStatistics(){
        endTime = Instant.now().getEpochSecond();
        endX = globalPlayerNonNull.get().getBlockPos().getX();
        Duration duration = Duration.ofSeconds(endTime - startTime);
        String sessionDuration = String.format("AutoWesternHighway session duration: %02d:%02d:%02d", duration.toHours(), duration.toMinutes() % 60, duration.toSeconds() % 60);
        System.out.println(sessionDuration) ;
        String sessionProgress = String.format("AutoWesternHighway session progress: %s blocks (from X = %d to X = %d)", abs(endX - startX), startX, endX);
        System.out.println(sessionProgress);
        startTime = 0;
        endTime = 0;
    }
}
