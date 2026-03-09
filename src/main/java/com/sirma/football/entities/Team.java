package com.sirma.football.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity @Table(name = "teams")
@Getter @Setter
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private Long legacyId;

    @NotBlank
    @Size(max = 50)
    @Column(length = 50)
    private String name;

    @NotBlank
    @Size(max = 50)
    @Column(length = 50)
    private String managerFullName;

    @NotBlank
    @Size(max = 5)
    @Column(length = 5)
    private String groupName;
}
