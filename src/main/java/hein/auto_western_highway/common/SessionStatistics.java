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

    public static String getSessionDurationString(){
        Duration duration = Duration.ofSeconds(endTime - startTime);
        return String.format("AWH session duration: %02d:%02d:%02d", duration.toHours(), duration.toMinutes() % 60, duration.toSeconds() % 60);
    }

    public static String getSessionProgressString(){
        return String.format("AWH session progress: %s blocks (from X = %d to X = %d)", abs(endX - startX), startX, endX);
    }

    public static void logSessionStatistics(){
        endTime = Instant.now().getEpochSecond();
        endX = globalPlayerNonNull.get().getBlockPos().getX();
        System.out.println(getSessionDurationString()) ;
        System.out.println(getSessionProgressString());
    }

    public static void resetSessionStatistics(){
        startTime = 0;
        endTime = 0;
    }
}
