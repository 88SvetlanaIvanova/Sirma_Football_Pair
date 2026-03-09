package com.sirma.football.mappers;

import com.sirma.football.model.dto.*;
import com.sirma.football.entities.*;

public final class CrudMappers {
    private CrudMappers() {}

    public static TeamDto toDto(Team t) {
        return new TeamDto(t.getId(), t.getName(), t.getManagerFullName(), t.getGroupName());
    }

    public static PlayerDto toDto(Player p) {
        return new PlayerDto(p.getId(), p.getTeam().getId(), p.getTeamNumber(), p.getPosition(), p.getFullName());
    }

    public static MatchDto toDto(Match m) {
        return new MatchDto(
                m.getId(),
                m.getTeamA().getId(),
                m.getTeamB().getId(),
                m.getDate(),
                m.getScore(),
                m.getBaseDuration()
        );
    }
}