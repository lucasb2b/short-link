package br.com.lucas.shortlink.dtos.request;

public record ResetPasswordRequestDTO(
        String token,
        String newPassword
) {
}
