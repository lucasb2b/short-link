package br.com.lucas.shortlink.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Resposta com os dados de uma imagem encurtada")
public record ImageResponseDTO(

        @Schema(description = "Nome original do arquivo", example = "foto.png")
        String originalFilename,

        @Schema(description = "Código curto gerado para o link", example = "abc123")
        String shortCode,

        @Schema(description = "Link curto do tremz.in para compartilhamento", example = "https://tremz.in/abc123")
        String shortUrl,

        @Schema(description = "URL direta do arquivo armazenado", example = "https://storage.tremz.in/images/abc123.png")
        String storageUrl,

        @Schema(description = "Lista de tags associadas à imagem", example = "[\"paisagem\", \"viagem\"]")
        List<String> tags,

        @Schema(description = "Data/hora de expiração do link (se aplicável)", example = "2025-12-31T23:59:59")
        LocalDateTime expiresAt,

        @Schema(description = "Indica se o upload foi anônimo (true → link expira)", example = "true")
        boolean isAnonymous,

        @Schema(description = "Tamanho do arquivo em bytes", example = "1048576")
        Long size,

        @Schema(description = "Data e hora de criação", example = "2025-12-31T23:59:59")
        LocalDateTime createdAt
) {}