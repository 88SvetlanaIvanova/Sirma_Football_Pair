package com.sirma.football.controller;

import com.sirma.football.services.PairAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {
    private final PairAnalysisService svc;

    @GetMapping(path = "/top/lines", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> lines() {
        return svc.top()
                .map(top -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append(top.p1()).append(", ")
                            .append(top.p2()).append(", ")
                            .append(top.totalMinutes()).append("\n");

                    svc.breakdown(top.p1(), top.p2()).forEach(pm ->
                            sb.append(pm.matchId()).append(", ")
                                    .append(pm.minutesTogether()).append("\n")
                    );

                    return ResponseEntity.ok(sb.toString());
                })
                .orElseGet(() -> ResponseEntity.ok("No data"));
    }
}