package com.cleevio.vexl.module.stats.dto;

import com.cleevio.vexl.module.stats.constant.StatsKey;

public record StatsDto(

        StatsKey key,
        int value
) {
}
