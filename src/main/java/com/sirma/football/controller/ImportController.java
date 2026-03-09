package com.sirma.football.controller;

import com.sirma.football.model.util.ImportReportDto;
import com.sirma.football.services.CsvImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportController {

    private final CsvImportService service;

    @PostMapping(value="/teams", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportReportDto importTeams(@RequestPart("file") MultipartFile file) throws IOException {
        try (var r = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            return service.importTeams(r);
        }
    }
    @PostMapping(value="/players", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportReportDto importPlayers(@RequestPart("file") MultipartFile file) throws IOException {
        try (var r = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            return service.importPlayers(r);
        }
    }

    @PostMapping(value="/matches", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportReportDto importMatches(@RequestPart("file") MultipartFile file) throws IOException {
        try (var r = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            return service.importMatches(r);
        }
    }

    @PostMapping(value="/appearances", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportReportDto importAppearances(@RequestPart("file") MultipartFile file) throws IOException {
        try (var r = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            return service.importAppearances(r);
        }
    }

    @PostMapping(value = "/debug", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> debug(@RequestHeader Map<String, String> headers,
                                     @RequestPart("file") MultipartFile file) throws IOException {

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("headers", headers);
        info.put("multipart", true);
        info.put("fileName", file.getOriginalFilename());
        info.put("fileSize", file.getSize());
        info.put("fileContentType", file.getContentType());
        return info;
    }

}

