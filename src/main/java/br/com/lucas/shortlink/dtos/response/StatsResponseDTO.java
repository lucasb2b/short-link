package br.com.lucas.shortlink.dtos.response;

import java.util.Map;

public record StatsResponseDTO(
        long totalLinks,
        long totalClicks,
        long totalPhotos,
        long trafficSavedBytes,
        Map<String, Long> topCountries,
        Map<String, Long> topBrowsers,
        Map<String, Long> topOS
) {}
