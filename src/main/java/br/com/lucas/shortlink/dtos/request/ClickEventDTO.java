package br.com.lucas.shortlink.dtos.request;

public record ClickEventDTO(
        String shortCode,
        String ipAddress,
        String userAgent
) {
}