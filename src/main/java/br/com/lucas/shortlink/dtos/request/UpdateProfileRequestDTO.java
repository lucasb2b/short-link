package br.com.lucas.shortlink.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateProfileRequestDTO(
        @Schema(description = "Novo nome", example = "Lucas Brito")
        String name,
        @Schema(description = "Nova URL do avatar", example = "https://...")
        String avatarUrl
) {}