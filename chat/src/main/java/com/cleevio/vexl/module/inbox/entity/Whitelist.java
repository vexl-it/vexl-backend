package com.cleevio.vexl.module.inbox.entity;

import com.cleevio.vexl.common.convertor.Sha256HashConvertor;
import com.cleevio.vexl.module.inbox.constant.WhitelistState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Table(name = "white_list")
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Whitelist {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Convert(converter = Sha256HashConvertor.class)
    private String publicKey;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private WhitelistState state;

    @JoinColumn(name = "inbox_id", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Inbox inbox;
}
