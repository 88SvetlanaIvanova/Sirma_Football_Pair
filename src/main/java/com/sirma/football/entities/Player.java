package com.sirma.football.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;

@Entity @Table(name = "players")
@Getter @Setter
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "legacy_id", unique = true)
    private Long legacyId;

    @NotNull
    @Min(1)
    private Integer teamNumber;

    @NotBlank
    private String position;

    @NotBlank
    @Size(max = 50)
    @Column(length = 50)
    private String fullName;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "team_id", nullable = false)
    private Team team;
}
