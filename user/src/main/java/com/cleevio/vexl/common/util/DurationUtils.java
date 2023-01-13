package com.cleevio.vexl.common.util;

import com.cleevio.vexl.module.cryptocurrency.constant.Duration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DurationUtils {

    public static long createFromUnixTimestamp(Duration duration) {
        switch (duration) {
            case DAY -> {
                return LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault()).toEpochSecond();
            }
            case HOUR -> {
                return LocalDateTime.now().minusHours(1).atZone(ZoneId.systemDefault()).toEpochSecond();
            }
            case WEEK -> {
                return LocalDateTime.now().minusWeeks(1).atZone(ZoneId.systemDefault()).toEpochSecond();
            }
            case MONTH -> {
                return LocalDateTime.now().minusMonths(1).atZone(ZoneId.systemDefault()).toEpochSecond();
            }
            case YEAR -> {
                return LocalDateTime.now().minusYears(1).atZone(ZoneId.systemDefault()).toEpochSecond();
            }
            case SIX_MONTHS -> {
                return LocalDateTime.now().minusMonths(6).atZone(ZoneId.systemDefault()).toEpochSecond();
            }
            case THREE_MONTHS -> {
                return LocalDateTime.now().minusMonths(3).atZone(ZoneId.systemDefault()).toEpochSecond();
            }
            default -> throw new IllegalArgumentException("Unknown duration: " + duration);
        }
    }
}
