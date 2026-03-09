package com.sirma.football.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import com.sirma.football.model.util.PairMatchDto;
import com.sirma.football.model.util.PairResultDto;

public final class RowMappers {
    private RowMappers(){}

    public static PairMatchDto pairMatchDto(ResultSet rs, int row) throws SQLException {
        return new PairMatchDto(
                rs.getLong("p1"),
                rs.getLong("p2"),
                rs.getLong("match_id"),
                rs.getInt("minutes_together")
        );
    }

    public static PairResultDto pairResultDto(ResultSet rs, int row) throws SQLException {
        return new PairResultDto(
                rs.getLong("p1"),
                rs.getLong("p2"),
                rs.getInt("total_minutes")
        );
    }

}