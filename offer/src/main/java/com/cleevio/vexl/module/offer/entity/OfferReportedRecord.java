package com.cleevio.vexl.module.offer.entity;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "offer_reported_record")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class OfferReportedRecord {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_public_key", nullable = false)
    private String userPublicKey;

    @Column(name = "reported_at", nullable = false)
    private Instant reportedAt;
}
