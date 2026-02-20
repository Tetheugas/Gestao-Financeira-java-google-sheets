package com.financeiro.controller;

import com.financeiro.dto.SpreadsheetIdDTO;
import com.financeiro.service.GoogleSheetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/spreadsheet")
@CrossOrigin(origins = "http://localhost:5173")
public class SpreadsheetController {

    @Autowired
    private GoogleSheetsService sheetsService;

    @GetMapping
    public ResponseEntity<SpreadsheetIdDTO> getSpreadsheetId() {
        String id = sheetsService.getSpreadsheetId();
        return ResponseEntity.ok(new SpreadsheetIdDTO(id));
    }
}
