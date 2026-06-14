package br.com.lucas.shortlink.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta de login contendo o token JWT")
public record LoginResponseDTO(
        @Schema(description = "Token JWT (Bearer)", example = "eyJhbGciOiJIUzI1NiJ9...")
        String token
) {}