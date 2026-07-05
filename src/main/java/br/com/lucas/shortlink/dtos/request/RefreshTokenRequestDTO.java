package br.com.lucas.shortlink.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Requisição para atualizar o token JWT")
public record RefreshTokenRequestDTO(
        @NotBlank(message = "O refreshToken não pode ser vazio")
        @Schema(description = "Refresh Token JWT")
        String refreshToken
) {}
