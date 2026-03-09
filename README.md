
# Football Pair Overlap – Spring Boot

## The algorithm
Find the pair of football players who played together the longest across all matches 
and list their per-match overlap minutes.

### Input
- `players.csv`: ID, TeamNumber, Position, FullName, TeamID
- `teams.csv`:   ID, Name, ManagerFullName, Group
- `matches.csv`: ID, ATeamID, BTeamID, Date, Score
- `records.csv`: ID, PlayerID, MatchID, fromMinutes, toMinutes

`toMinutes = NULL` means “end of match” (90’ by default).

### Output

playerId1, playerId2, totalMinutes
matchId, minutes
matchId, minutes
...
### Understanding & Algorithm

1. **Normalize intervals**  
   For each appearance, resolve `effectiveTo = COALESCE(toMinutes, match.baseDuration, 90)`. 
   matchEnd defaults to 90 minutes or uses the match's baseDuration if provided.

2. **Only same team**  
   Players are considered “together” only if they belong to the same team **and** that team participates in the match (A or B).

3. **Overlap per match**  
   For any two players on the same team, the overlap of two intervals `(a.from, a.to)` and `(b.from, b.to)` is  
   `max(0, min(a.to, b.to) - max(a.from, b.from))`.  
   Sum across multiple stints per player within that match.

4. **Aggregation**  
   - Per match: sum of overlaps for that pair (across all stint combinations).
   - Global: sum per pair across all their common matches.
   - Select the pair with the **maximum global** sum.

5. **Implementation**  
   - **SQL approach**: resolve `effectiveTo` with `COALESCE`, self-join `appearances` on same `match_id` and `team_id` with `player_id1 < player_id2`, compute overlap via `LEAST`/`GREATEST`, group & aggregate.
  

6. **Edge cases**  
   - Zero or negative overlaps ignored.  
   - Missing `toMinutes` treated as end of match.  
   - Multiple stints per player supported.  
   - Invalid rows (bad IDs, impossible ranges) are logged and skipped.

### How to run
- `POST /api/import/*` to load CSVs.
- `GET /api/analysis/top-pair` returns:json
{
  "player1Id": 113,
  "player2Id": 128,
  "totalMinutes": 84,
  "perMatch": [ { "matchId": 101, "minutes": 52 }, { "matchId": 120, "minutes": 32 } ]
}

### Build & run

Add spring-boot-docker-compose and a compose.yaml with postgres:16-alpine. App will auto-start DB on run.
Or run Postgres yourself and set SPRING_DATASOURCE_*.

Build
mvn -N wrapper:wrapper
./mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=football-pairs:latest
docker run -p 8080:8080 football-pairs:latest

mvn clean spring-boot:run

### Tests
The test suite covers the application end-to-end, excluding data formats:

Postman collection is included.
CSV Import Pipeline – Parsing, validation, duplicate detection, reference checks, and persistence for teams, 
players, matches, and appearances.
CRUD REST Controllers – Team, player, and match endpoints are tested for correct JSON output, status codes, and Location headers.
Pair Overlap Algorithm – Integration tests load real CSV data and verify the SQL logic for finding the top player pair and their total shared minutes.
Services & Repositories – Unit tests confirm delegation, filtering, and lookup behavior across the service and repository layers.
