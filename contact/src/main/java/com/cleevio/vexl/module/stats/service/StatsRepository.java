package com.cleevio.vexl.module.stats.service;

import com.cleevio.vexl.module.stats.entity.Stats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatsRepository extends JpaRepository<Stats, Long> {
}
