package com.financeiro.controller;

import com.financeiro.dto.MessageDTO;
import com.financeiro.dto.SpreadsheetIdDTO;
import com.financeiro.service.GoogleSheetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

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

    @PutMapping
    public ResponseEntity<MessageDTO> updateSpreadsheetId(@RequestBody SpreadsheetIdDTO dto) {
        try {
            sheetsService.setSpreadsheetId(dto.getSpreadsheetId());
            return ResponseEntity.ok(new MessageDTO("Spreadsheet ID atualizado com sucesso", "success"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageDTO(e.getMessage(), "error"));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(new MessageDTO("Erro ao salvar Spreadsheet ID", "error"));
        }
    }

    @PostMapping
    public ResponseEntity<SpreadsheetIdDTO> createNewSpreadsheet() {
        try {
            String newId = sheetsService.createNewSpreadsheet();
            return ResponseEntity.status(201).body(new SpreadsheetIdDTO(newId));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
