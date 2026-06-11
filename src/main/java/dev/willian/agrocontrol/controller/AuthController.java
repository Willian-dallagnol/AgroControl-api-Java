package dev.willian.agrocontrol.controller;

import dev.willian.agrocontrol.dto.UserResponse;
import dev.willian.agrocontrol.dto.auth.AuthResponse;
import dev.willian.agrocontrol.dto.auth.LoginRequest;
import dev.willian.agrocontrol.dto.auth.RegisterRequest;
import dev.willian.agrocontrol.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticacao", description = "Registro, login e dados do usuario autenticado")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Registra um novo usuario e retorna um token JWT")
    @SecurityRequirements // rota publica: nao exige o cadeado do Swagger
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @Operation(summary = "Autentica um usuario existente e retorna um token JWT")
    @SecurityRequirements // rota publica: nao exige o cadeado do Swagger
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Retorna o usuario dono do token atual")
    @GetMapping("/me")
    public UserResponse me() {
        return authService.me();
    }
}
