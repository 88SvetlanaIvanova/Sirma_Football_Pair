package com.sirma.football.model.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record PlayerCreateDto(
        @NotNull UUID teamId,
        @NotNull @Min(1) Integer teamNumber,
        @NotBlank String position,
        @NotBlank @Size(max = 50) String fullName
) {}
