package com.cleevio.vexl.module.offer.entity;

import com.cleevio.vexl.common.convertor.AesEncryptionConvertor;
import com.cleevio.vexl.module.offer.constant.OfferType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "offer_public")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class OfferPublicPart {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Exclude
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    @Convert(converter = AesEncryptionConvertor.class)
    @ToString.Exclude
    private String adminId;

    @Column(nullable = false, updatable = false)
    @ToString.Exclude
    private String offerId;

    @CreationTimestamp
    private LocalDate createdAt;

    private LocalDate modifiedAt;

    @Column(nullable = false)
    private LocalDate refreshedAt;

    @Enumerated(EnumType.STRING)
    private OfferType offerType;

    @Column(nullable = false)
    @ToString.Exclude
    private int report;

    private Integer countryPrefix;

    @ToString.Exclude
    private String payloadPublic;

    @ToString.Exclude
    @OneToMany(mappedBy = "offerPublicPart", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<OfferPrivatePart> offerPrivateParts = new HashSet<>();

}
