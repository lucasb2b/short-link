package br.com.lucas.shortlink.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados necessários para registrar um novo usuário")
public record RegisterRequestDTO(
        @Schema(description = "Nome completo do usuário", example = "Lucas Brito", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String name,

        @Schema(description = "E-mail único do usuário", example = "lucas@email.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Email
        String email,

        @Schema(description = "Senha com no mínimo 8 caracteres", example = "Senha@123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(min = 8)
        String password
) {}