package br.com.lucas.shortlink.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para redefinir a senha (fluxo de recuperação)")
public record ResetPasswordRequestDTO(

        @Schema(description = "Token de recuperação enviado por e-mail", example = "abc123token", requiredMode = Schema.RequiredMode.REQUIRED)
        String token,

        @Schema(description = "Nova senha", example = "NovaSenha@789", requiredMode = Schema.RequiredMode.REQUIRED)
        String newPassword
) {
}