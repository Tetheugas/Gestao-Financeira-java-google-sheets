package com.financeiro.controller;

import com.financeiro.service.GoogleSheetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    @Autowired
    private GoogleSheetsService sheetsService;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> getStatus() throws IOException {
        boolean authorized = sheetsService.isAuthorized();
        return ResponseEntity.ok(Collections.singletonMap("authenticated", authorized));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login() {
        try {
            // Check if already authorized
            if (sheetsService.isAuthorized()) {
                return ResponseEntity.ok(Collections.singletonMap("message", "Already authenticated"));
            }

            // Get the auth URL
            String url = sheetsService.getAuthorizationUrl();
            return ResponseEntity.ok(Collections.singletonMap("url", url));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Collections.singletonMap("error", "Error starting auth flow: " + e.getMessage()));
        }
    }
}
