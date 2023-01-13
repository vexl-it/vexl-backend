package com.cleevio.vexl.module.stats.entity;

import com.cleevio.vexl.module.stats.constant.StatsKey;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Table
@Entity
@Data
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Stats {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    @Getter(value = AccessLevel.PRIVATE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatsKey key;

    @Column(nullable = false)
    private int value;

    @CreationTimestamp
    @Setter(AccessLevel.PRIVATE)
    @Column(nullable = false)
    private LocalDateTime date;

    public Stats(StatsKey key, int value) {
        this.key = key;
        this.value = value;
    }
}
