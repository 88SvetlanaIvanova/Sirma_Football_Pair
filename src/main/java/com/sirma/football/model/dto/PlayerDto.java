package com.sirma.football.model.dto;
import java.util.UUID;

public record PlayerDto(
        UUID id, UUID teamId, Integer teamNumber, String position, String fullName
) {}