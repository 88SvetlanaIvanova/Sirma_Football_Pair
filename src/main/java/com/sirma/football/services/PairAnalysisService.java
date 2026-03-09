package com.sirma.football.services;

import com.sirma.football.model.util.PairMatchDto;
import com.sirma.football.model.util.PairResultDto;
import com.sirma.football.repositories.PairAnalysisRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.List;

@Service
public class PairAnalysisService {
    private final PairAnalysisRepository repo;

    public PairAnalysisService(PairAnalysisRepository repo) {
        this.repo = repo;
    }

    public Optional<PairResultDto> top() {
        return repo.findTopPair();
    }

    public List<PairMatchDto> breakdown(Long p1Legacy, Long p2Legacy) {
        return repo.findByMatchForPair(p1Legacy, p2Legacy);
    }
}

