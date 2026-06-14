package br.com.lucas.shortlink.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de um evento de clique em um link encurtado")
public record ClickEventDTO(

        @Schema(description = "Código curto do link acessado", example = "abc123", requiredMode = Schema.RequiredMode.REQUIRED)
        String shortCode,

        @Schema(description = "Endereço IP do visitante", example = "192.168.0.1", requiredMode = Schema.RequiredMode.REQUIRED)
        String ipAddress,

        @Schema(description = "User-Agent do navegador/dispositivo", example = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36", requiredMode = Schema.RequiredMode.REQUIRED)
        String userAgent
) {
}