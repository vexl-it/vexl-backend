package com.cleevio.vexl.module.push.entity;

import com.cleevio.vexl.common.hibernate.type.SetArrayType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Set;


@TypeDefs({
        @TypeDef(
                name = "set-array",
                typeClass = SetArrayType.class
        )
})
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Immutable
public class Push {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String groupUuid;

    @Type(type = "set-array")
    @Column(
            name = "firebase_token",
            columnDefinition = "text[]",
            updatable = false
    )
    private Set<String> firebaseTokens;

    public Push(String groupUuid, Set<String> firebaseTokens) {
        this.groupUuid = groupUuid;
        this.firebaseTokens = firebaseTokens;
    }
}
