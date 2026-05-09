package br.com.lucas.shortlink.dtos.response;

public record LinkResponseDTO(
    String originalUrl,
    String shortUrl
) {
}
