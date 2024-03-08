package com.cleevio.vexl.module.message.entity;

import com.cleevio.vexl.common.convertor.AesEncryptionConvertor;
import com.cleevio.vexl.module.message.constant.MessageType;
import com.cleevio.vexl.module.inbox.entity.Inbox;
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

@Table
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Message {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    @Convert(converter = AesEncryptionConvertor.class)
    private String senderPublicKey;

    @Column(nullable = false)
    private boolean pulled;

    @Column(nullable = false)
    private String type;

    @JoinColumn(name = "inbox_id", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Inbox inbox;
}
