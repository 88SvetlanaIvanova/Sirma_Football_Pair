package com.sirma.football.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity @Table(name = "matches")
@Getter @Setter
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "legacy_id", unique = true)
    private Long legacyId;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "a_team_id")
    private Team teamA;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "b_team_id")
    private Team teamB;

    @NotNull
    private LocalDate date;

    @NotBlank
    private String score;

    @NotNull
    private Integer baseDuration = 90;
}
