package com.cleevio.vexl.module.stats.mapper;

import com.cleevio.vexl.module.stats.dto.StatsDto;
import com.cleevio.vexl.module.stats.entity.Stats;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StatsMapper {

    public List<Stats> mapList(final List<StatsDto> statsDtos) {
        return statsDtos.stream()
                .map(this::mapSingle)
                .toList();
    }

    private Stats mapSingle(final StatsDto statsDto) {
        return new Stats(
                statsDto.key(),
                statsDto.value()
        );
    }
}
