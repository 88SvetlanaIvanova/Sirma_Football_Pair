package com.sirma.football.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Entity @Table(name = "appearances")
@Getter @Setter
public class Appearance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "legacy_id", unique = true)
    private Long legacyId;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "match_id")
    private Match match;

    @NotNull
    private Integer fromMinute;
    private Integer toMinute;
}
