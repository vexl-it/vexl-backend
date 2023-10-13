package com.cleevio.vexl.module.feedback.entity;

import lombok.*;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "feedback_submit")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class FeedbackSubmit {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(updatable = false, nullable = false)
    private String formId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private LocalDate lastUpdate;

    @Nullable
    private Integer stars;

    @Nullable
    private String objections;

    @Nullable
    private String textComment;

}
