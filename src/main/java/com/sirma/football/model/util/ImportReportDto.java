package com.sirma.football.model.util;

public record ImportReportDto(
        int teamsImported,
        int playersImported,
        int matchesImported,
        int appearancesImported
) {}