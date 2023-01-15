package com.cleevio.vexl.module.group.entity;

import com.cleevio.vexl.common.convertor.AesEncryptionConvertor;
import it.vexl.common.crypto.CryptoLibrary;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@Table(name = "\"group\"")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Group {

    @Id
    @EqualsAndHashCode.Include
    @Getter(value = AccessLevel.PRIVATE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @EqualsAndHashCode.Include
    @Column(updatable = false, nullable = false)
    private final String uuid = CryptoLibrary.instance.sha256(UUID.randomUUID().toString());

    @Column(updatable = false, nullable = false)
    @Convert(converter = AesEncryptionConvertor.class)
    private String name;

    @Nullable
    @Convert(converter = AesEncryptionConvertor.class)
    private String logoUrl;

    @Column(updatable = false, nullable = false)
    @Convert(converter = AesEncryptionConvertor.class)
    private String qrCodeUrl;

    @Column(updatable = false, nullable = false)
    private final long createdAt = Instant.now().getEpochSecond();

    @Column(updatable = false, nullable = false)
    private long expirationAt;

    @Column(updatable = false, nullable = false)
    private long closureAt;

    @Column(updatable = false, nullable = false)
    private int code;

    @Column(updatable = false, nullable = false)
    private String createdBy;
}
