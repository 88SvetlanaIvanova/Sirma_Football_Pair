package com.sirma.football.services;

import com.sirma.football.model.util.ImportReportDto;
import com.sirma.football.entities.Appearance;
import com.sirma.football.entities.Match;
import com.sirma.football.entities.Player;
import com.sirma.football.entities.Team;
import com.sirma.football.repositories.AppearanceRepository;
import com.sirma.football.repositories.MatchRepository;
import com.sirma.football.repositories.PlayerRepository;
import com.sirma.football.repositories.TeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class CsvImportService {

    private static final int BATCH_SIZE = 1000;
    private static final Logger log = LoggerFactory.getLogger(CsvImportService.class);
    private final TeamRepository teamRepo;
    private final PlayerRepository playerRepo;
    private final MatchRepository matchRepo;
    private final AppearanceRepository appRepo;

    public CsvImportService(TeamRepository teamRepo,
                            PlayerRepository playerRepo,
                            MatchRepository matchRepo,
                            AppearanceRepository appRepo) {
        this.teamRepo = teamRepo;
        this.playerRepo = playerRepo;
        this.matchRepo = matchRepo;
        this.appRepo = appRepo;
    }

    @Transactional
    public ImportReportDto importTeams(Reader reader) {
        int imported = 0;
        List<Team> toSave = new ArrayList<>(BATCH_SIZE);

        try (BufferedReader br = new BufferedReader(reader, 64 * 1024)) {
            String line = firstNonNullLine(br);
            if (line == null) return new ImportReportDto(0, 0, 0, 0);

            if (isHeader(line)) imported += processTeamRow(parse(line), 1, toSave);

            int lineNo = 1;
            for (String l; (l = br.readLine()) != null; ) {
                lineNo++;
                if (ignorable(l)) continue;
                imported += processTeamRow(parse(l), lineNo, toSave);
                if (toSave.size() >= BATCH_SIZE) { teamRepo.saveAll(toSave); toSave.clear(); }
            }
            if (!toSave.isEmpty()) teamRepo.saveAll(toSave);

        } catch (IOException e) {
            log.error("Teams import IO error", e);
        }
        return new ImportReportDto(imported, 0, 0, 0);
    }

    private int processTeamRow(List<String> cols, int lineNo, List<Team> batch) {
        try {
            requireCols(cols, 4, lineNo);
            Long legacyId = parseLongReq(cols.get(0), "ID", lineNo);
            String name    = cols.get(1).trim();
            String manager = cols.get(2).trim();
            String group   = cols.get(3).trim();

            if (teamRepo.findByLegacyId(legacyId).isPresent()) return 0;

            Team t = new Team();
            t.setLegacyId(legacyId);
            t.setName(name);
            t.setManagerFullName(manager);
            t.setGroupName(group);

            batch.add(t);
            return 1;
        } catch (Exception ex) {
            log.warn("Teams: {}", ex.getMessage());
            return 0;
        }
    }

    @Transactional
    public ImportReportDto importPlayers(Reader reader) {
        int imported = 0;
        List<Player> toSave = new ArrayList<>(BATCH_SIZE);

        try (BufferedReader br = new BufferedReader(reader, 64 * 1024)) {
            String line = firstNonNullLine(br);
            if (line == null) return new ImportReportDto(0, 0, 0, 0);

            if (isHeader(line)) imported += processPlayerRow(parse(line), 1, toSave);

            int lineNo = 1;
            for (String l; (l = br.readLine()) != null; ) {
                lineNo++;
                if (ignorable(l)) continue;
                imported += processPlayerRow(parse(l), lineNo, toSave);
                if (toSave.size() >= BATCH_SIZE) { playerRepo.saveAll(toSave); toSave.clear(); }
            }
            if (!toSave.isEmpty()) playerRepo.saveAll(toSave);

        } catch (IOException e) {
            log.error("Players import IO error", e);
        }
        return new ImportReportDto(0, imported, 0, 0);
    }

    private int processPlayerRow(List<String> cols, int lineNo, List<Player> batch) {
        try {
            requireCols(cols, 5, lineNo);

            Long legacyId     = parseLongReq(cols.get(0), "ID", lineNo);
            Integer teamNo    = parseIntReq(cols.get(1), "TeamNumber", lineNo);
            String position   = cols.get(2).trim();
            String fullName   = cols.get(3).trim();
            Long teamLegacyId = parseLongReq(cols.get(4), "TeamID", lineNo);

            if (playerRepo.findByLegacyId(legacyId).isPresent()) return 0;

            Team team = teamRepo.findByLegacyId(teamLegacyId)
                    .orElseThrow(() -> new IllegalArgumentException("Line " + lineNo + ": TeamID " + teamLegacyId + " not found"));

            Player p = new Player();
            p.setLegacyId(legacyId);
            p.setTeamNumber(teamNo);
            p.setPosition(position);
            p.setFullName(fullName);
            p.setTeam(team);

            batch.add(p);
            return 1;
        } catch (Exception ex) {
            log.warn("Players: {}", ex.getMessage());
            return 0;
        }
    }

    @Transactional
    public ImportReportDto importMatches(Reader reader) {
        int imported = 0;
        List<Match> toSave = new ArrayList<>(BATCH_SIZE);

        try (BufferedReader br = new BufferedReader(reader, 64 * 1024)) {
            String line = firstNonNullLine(br);
            if (line == null) return new ImportReportDto(0, 0, 0, 0);

            if (isHeader(line)) imported += processMatchRow(parse(line), 1, toSave);

            int lineNo = 1;
            for (String l; (l = br.readLine()) != null; ) {
                lineNo++;
                if (ignorable(l)) continue;
                imported += processMatchRow(parse(l), lineNo, toSave);
                if (toSave.size() >= BATCH_SIZE) { matchRepo.saveAll(toSave); toSave.clear(); }
            }
            if (!toSave.isEmpty()) matchRepo.saveAll(toSave);

        } catch (IOException e) {
            log.error("Matches import IO error", e);
        }
        return new ImportReportDto(0, 0, imported, 0);
    }

    private int processMatchRow(List<String> cols, int lineNo, List<Match> batch) {
        try {
            requireCols(cols, 5, lineNo);

            Long legacyId  = parseLongReq(cols.get(0), "ID", lineNo);
            Long aLegacyId = parseLongReq(cols.get(1), "ATeamID", lineNo);
            Long bLegacyId = parseLongReq(cols.get(2), "BTeamID", lineNo);
            String dateRaw = cols.get(3).trim();
            String score   = cols.get(4).trim();

            if (matchRepo.findByLegacyId(legacyId).isPresent()) return 0;

            Team a = teamRepo.findByLegacyId(aLegacyId)
                    .orElseThrow(() -> new IllegalArgumentException("Line " + lineNo + ": ATeamID " + aLegacyId + " not found"));
            Team b = teamRepo.findByLegacyId(bLegacyId)
                    .orElseThrow(() -> new IllegalArgumentException("Line " + lineNo + ": BTeamID " + bLegacyId + " not found"));

            LocalDate date = DateParsingUtil.parseFlexible(dateRaw);

            Match m = new Match();
            m.setLegacyId(legacyId);
            m.setTeamA(a);
            m.setTeamB(b);
            m.setDate(date);
            m.setScore(score);

            batch.add(m);
            return 1;
        } catch (Exception ex) {
            log.warn("Matches: {}", ex.getMessage());
            return 0;
        }
    }

    @Transactional
    public ImportReportDto importAppearances(Reader reader) {
        int imported = 0;
        List<Appearance> toSave = new ArrayList<>(Math.max(BATCH_SIZE, 2000));

        try (BufferedReader br = new BufferedReader(reader, 64 * 1024)) {
            String line = firstNonNullLine(br);
            if (line == null) return new ImportReportDto(0, 0, 0, 0);

            if (isHeader(line)) imported += processAppearanceRow(parse(line), 1, toSave);

            int lineNo = 1;
            for (String l; (l = br.readLine()) != null; ) {
                lineNo++;
                if (ignorable(l)) continue;
                imported += processAppearanceRow(parse(l), lineNo, toSave);
                if (toSave.size() >= BATCH_SIZE) { appRepo.saveAll(toSave); toSave.clear(); }
            }
            if (!toSave.isEmpty()) appRepo.saveAll(toSave);

        } catch (IOException e) {
            log.error("Appearances import IO error", e);
        }
        return new ImportReportDto(0, 0, 0, imported);
    }

    private int processAppearanceRow(List<String> cols, int lineNo, List<Appearance> batch) {
        try {
            requireCols(cols, 5, lineNo);

            Long legacyId      = parseLongReq(cols.get(0), "ID", lineNo);
            Long playerLegacy  = parseLongReq(cols.get(1), "PlayerID", lineNo);
            Long matchLegacy   = parseLongReq(cols.get(2), "MatchID", lineNo);
            Integer fromM      = parseIntReq(cols.get(3), "fromMinutes", lineNo);
            Integer toM        = parseNullableInt(cols.get(4));

            if (fromM < 0) throw new IllegalArgumentException("Line " + lineNo + ": fromMinutes < 0");
            if (toM != null && toM <= fromM)
                throw new IllegalArgumentException("Line " + lineNo + ": toMinutes <= fromMinutes");

            Player player = playerRepo.findByLegacyId(playerLegacy)
                    .orElseThrow(() -> new IllegalArgumentException("Line " + lineNo + ": PlayerID " + playerLegacy + " not found"));
            Match match = matchRepo.findByLegacyId(matchLegacy)
                    .orElseThrow(() -> new IllegalArgumentException("Line " + lineNo + ": MatchID " + matchLegacy + " not found"));

            Appearance ap = new Appearance();
            ap.setLegacyId(legacyId);
            ap.setPlayer(player);
            ap.setMatch(match);
            ap.setFromMinute(fromM);
            ap.setToMinute(toM);

            batch.add(ap);
            return 1;
        } catch (Exception ex) {
            log.warn("Appearances: {}", ex.getMessage());
            return 0;
        }
    }

    @Transactional
    public ImportReportDto importAll(Reader reader) {
        return new ImportReportDto(0, 0, 0, 0);
    }

    private static String firstNonNullLine(BufferedReader br) throws IOException {
        String line = br.readLine();
        if (line == null) return null;
        line = stripBom(line);
        while (ignorable(line)) {
            line = br.readLine();
            if (line == null) return null;
        }
        return line;
    }

    private static boolean ignorable(String line) {
        String s = (line == null) ? "" : line.trim();
        return s.isEmpty() || s.startsWith("#");
    }

    private static boolean isHeader(String line) {
        List<String> cols = parse(line);
        if (cols.isEmpty()) return false;
        String first = cols.getFirst().trim();
        return !first.equalsIgnoreCase("id") && first.matches("\\d+");
    }

    private static void requireCols(List<String> cols, int expected, int lineNo) {
        if (cols.size() < expected) {
            throw new IllegalArgumentException("Line " + lineNo + ": expected " + expected + " columns, got " + cols.size());
        }
    }

    private static Long parseLongReq(String raw, String field, int lineNo) {
        try { return Long.parseLong(raw.trim()); }
        catch (Exception e) { throw new IllegalArgumentException("Line " + lineNo + ": invalid " + field + "='" + raw + "'"); }
    }

    private static Integer parseIntReq(String raw, String field, int lineNo) {
        try { return Integer.parseInt(raw.trim()); }
        catch (Exception e) { throw new IllegalArgumentException("Line " + lineNo + ": invalid " + field + "='" + raw + "'"); }
    }

    private static Integer parseNullableInt(String raw) {
        String s = raw == null ? null : raw.trim();
        if (s == null || s.isEmpty() || s.equalsIgnoreCase("null")) return null;
        return Integer.parseInt(s);
    }

    private static List<String> parse(String line) {
        List<String> out = new ArrayList<>();
        if (line == null) return out;
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0, n = line.length(); i < n; i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < n && line.charAt(i + 1) == '"') {
                    sb.append('"'); // escaped double quote
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                out.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        out.add(sb.toString());
        return out;
    }

    private static String stripBom(String s) {
        if (s != null && !s.isEmpty() && s.charAt(0) == '\ufeff') {
            return s.substring(1);
        }
        return s;
    }

    private static UUID requireMapped(Map<Long, UUID> map, Long legacyId, String field, int lineNo) {
        UUID uuid = map.get(legacyId);
        if (uuid == null) {
            throw new IllegalArgumentException("Line " + lineNo + ": no UUID mapping for " + field + "=" + legacyId +
                    " (import Teams/Players/Matches first, in that order)");
        }
        return uuid;
    }

    private static final class DateParsingUtil {
        private static final DateTimeFormatter[] SUPPORTED = new DateTimeFormatter[] {
                DateTimeFormatter.ISO_LOCAL_DATE,            // 2024-06-14
                DateTimeFormatter.ofPattern("M/d/uuuu"),     // 6/14/2024
                DateTimeFormatter.ofPattern("d/M/uuuu"),     // 14/6/2024
                DateTimeFormatter.ofPattern("dd.MM.uuuu"),   // 14.06.2024
                DateTimeFormatter.ofPattern("uuuuMMdd")      // 20240614
        };
        static LocalDate parseFlexible(String raw) {
            String s = (raw == null) ? "" : raw.trim();
            for (DateTimeFormatter f : SUPPORTED) {
                try { return LocalDate.parse(s, f); }
                catch (DateTimeParseException ignored) {}
            }
            throw new IllegalArgumentException("Unsupported date format: '" + raw + "'");
        }
    }
}