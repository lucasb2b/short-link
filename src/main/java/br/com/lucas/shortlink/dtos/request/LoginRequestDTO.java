package br.com.lucas.shortlink.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Credenciais para autenticação")
public record LoginRequestDTO(
        @Schema(example = "lucas@email.com") String email,
        @Schema(example = "Senha@123")        String password
) {}