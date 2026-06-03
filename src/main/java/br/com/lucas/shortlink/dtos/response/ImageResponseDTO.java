package br.com.lucas.shortlink.dtos.response;

import java.time.LocalDateTime;
import java.util.List;

public record ImageResponseDTO(
        String originalFilename,
        String shortCode,
        String shortUrl, // O link final do tremz.in para compartilhar
        String storageUrl, // A url direta do arquivo
        List<String> tags,
        LocalDateTime expiresAt,
        boolean isAnonymous // Flag para o frontend saber se a imagem vai expirar
) {}