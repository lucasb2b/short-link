package br.com.lucas.shortlink.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Formato padrão de erro retornado pela API")
public record ErrorResponseDTO(
        @Schema(example = "2025-01-01T12:00:00") LocalDateTime timestamp,
        @Schema(example = "400")                  int status,
        @Schema(example = "Bad Request")          String error,
        @Schema(example = "E-mail já cadastrado") String message,
        @Schema(example = "/v1/auth/register")    String path
) {}