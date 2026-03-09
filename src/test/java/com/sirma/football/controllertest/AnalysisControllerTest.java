package com.sirma.football.controllertest;

import com.sirma.football.controller.AnalysisController;
import com.sirma.football.services.PairAnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AnalysisControllerTest {

    private MockMvc mvc;
    private PairAnalysisService svc;

    @BeforeEach
    void setup() {
        svc = Mockito.mock(PairAnalysisService.class);
        mvc = MockMvcBuilders
                .standaloneSetup(new AnalysisController(svc))
                .build();
    }

    @Test
    void lines_returnsNoData_whenEmpty() throws Exception {
        given(svc.top()).willReturn(Optional.empty());

        mvc.perform(get("/api/analysis/top/lines"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("No data"));
    }
}
