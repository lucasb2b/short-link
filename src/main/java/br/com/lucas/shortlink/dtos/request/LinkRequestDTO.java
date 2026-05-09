package br.com.lucas.shortlink.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record LinkRequestDTO(
        @NotBlank(message = "O campo é obrigatório.")
        String originalUrl
) {
}
