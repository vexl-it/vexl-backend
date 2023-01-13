package com.cleevio.vexl.module.inbox.entity;

import com.cleevio.vexl.common.convertor.AesEncryptionConvertor;
import com.cleevio.vexl.common.convertor.Sha256HashConvertor;
import com.cleevio.vexl.module.inbox.constant.Platform;
import com.cleevio.vexl.module.message.entity.Message;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Table
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Inbox {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    @Getter(value = AccessLevel.PRIVATE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Convert(converter = Sha256HashConvertor.class)
    private String publicKey;

    @Convert(converter = AesEncryptionConvertor.class)
    private String token;

    @Enumerated(EnumType.STRING)
    private Platform platform;

    @OneToMany(mappedBy = "inbox", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Message> messages = new ArrayList<>();

    @OneToMany(mappedBy = "inbox", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Whitelist> whitelists = new HashSet<>();
}
