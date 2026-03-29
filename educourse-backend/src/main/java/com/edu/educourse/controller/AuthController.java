package com.edu.educourse.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.edu.educourse.dto.AuthResponse;
import com.edu.educourse.dto.LoginRequest;
import com.edu.educourse.dto.RegisterRequest;
import com.edu.educourse.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthService authService;

    /** POST /api/auth/register — create a new STUDENT account */
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(
            "Account created successfully for: " + request.getEmail()
        );
    }

    /** POST /api/auth/login — authenticate by email + password, returns JWT */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
