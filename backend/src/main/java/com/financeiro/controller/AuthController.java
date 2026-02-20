package com.financeiro.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> getStatus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean authorized = authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());

        return ResponseEntity.ok(Collections.singletonMap("authenticated", authorized));
    }
}
