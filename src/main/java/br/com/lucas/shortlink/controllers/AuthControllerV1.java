package br.com.lucas.shortlink.controllers;

import br.com.lucas.shortlink.dtos.request.*;
import br.com.lucas.shortlink.dtos.response.ErrorResponseDTO;
import br.com.lucas.shortlink.dtos.response.LoginResponseDTO;
import br.com.lucas.shortlink.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Endpoints de autenticação e gerenciamento de conta")
public class AuthControllerV1 {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário",
            description = "Cria a conta e envia e-mail de verificação (válido por 24h).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado"),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequestDTO request) {
        authService.register(request);
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário", description = "Retorna um token JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login bem-sucedido",
                    content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "E-mail não verificado ou conta inativa",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        String token = authService.login(request);
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Confirmar e-mail", description = "Valida o token enviado por e-mail.")
    public ResponseEntity<Void> verifyEmail(
            @Parameter(description = "Token enviado por e-mail", required = true)
            @RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar reset de senha",
            description = "Envia e-mail com link de reset (válido por 30 minutos).")
    public ResponseEntity<Void> forgotPassword(@RequestParam String email) {
        authService.forgotPassword(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Efetivar reset de senha")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        authService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/change-password")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Alterar senha (autenticado)")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequestDTO request) {
        authService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/profile")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Atualizar perfil do usuário")
    public ResponseEntity<Void> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequestDTO request) {
        authService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/deactivate")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Desativar conta")
    public ResponseEntity<Void> deactivate(@AuthenticationPrincipal UserDetails userDetails) {
        authService.deactivateAccount(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}