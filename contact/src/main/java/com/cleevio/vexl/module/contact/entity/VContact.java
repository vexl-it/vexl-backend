package com.cleevio.vexl.module.contact.entity;

import com.cleevio.vexl.module.contact.constant.ConnectionLevel;
import lombok.Data;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "v_contact")
@Data
@Immutable
public class VContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "public_key")
    private String publicKey;

    @Column(name = "my_public_key")
    private String myPublicKey;

    @Column(name = "level")
    @Enumerated(EnumType.STRING)
    private ConnectionLevel level;
}
