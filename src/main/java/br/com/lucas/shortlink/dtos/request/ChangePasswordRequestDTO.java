package br.com.lucas.shortlink.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados necessários para alterar a senha do usuário autenticado")
public record ChangePasswordRequestDTO(

        @Schema(description = "Senha atual do usuário", example = "Senha@123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "A senha atual é obrigatória")
        String currentPassword,

        @Schema(description = "Nova senha com no mínimo 6 caracteres", example = "NovaSenha@456", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "A nova senha é obrigatória")
        @Size(min = 6, message = "A nova senha deve ter no mínimo 6 caracteres")
        String newPassword
) {
}