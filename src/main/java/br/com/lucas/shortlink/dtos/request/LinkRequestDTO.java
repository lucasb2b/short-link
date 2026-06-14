package br.com.lucas.shortlink.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

@Schema(description = "Dados para encurtar uma URL")
public record LinkRequestDTO(

        @Schema(description = "URL original a ser encurtada", example = "https://exemplo.com/pagina/longa", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "URL é obrigatória")
        @URL(message = "URL inválida")
        String originalUrl
) {
}