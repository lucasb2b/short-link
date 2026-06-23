package br.com.lucas.shortlink.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Resposta com os dados do link encurtado")
public record LinkResponseDTO(

        @Schema(description = "URL original fornecida", example = "https://exemplo.com/pagina/longa")
        String originalUrl,

        @Schema(description = "Link curto gerado para compartilhamento", example = "https://tremz.in/abc123")
        String shortUrl,

        @Schema(description = "Código único que representa o link curto", example = "abc123")
        String shortCode,

        @Schema(description = "Quantidade de acessos no link", example = "3")
        long clicks,

        @Schema(description = "Data de criação do link", example = "2026-04-24T10:34")
        LocalDateTime createdAt
) {
}