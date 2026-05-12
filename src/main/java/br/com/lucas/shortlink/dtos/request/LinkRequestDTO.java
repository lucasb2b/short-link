package br.com.lucas.shortlink.dtos.request;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record LinkRequestDTO(
        @NotBlank(message = "URL é obrigatória")
        @URL(message = "URL inválida")
        String originalUrl
) {
}
