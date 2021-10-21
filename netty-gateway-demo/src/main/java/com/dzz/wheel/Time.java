package com.dzz.wheel;

import java.util.concurrent.TimeUnit;

public class Time {

    public static Long getHiresClockMs() {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
    }
}
