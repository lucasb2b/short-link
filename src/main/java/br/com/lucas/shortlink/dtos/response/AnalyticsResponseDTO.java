package br.com.lucas.shortlink.dtos.response;

import java.util.Map;

public record AnalyticsResponseDTO(
        long totalClicks,
        Map<String, Long> browsers,
        Map<String, Long> operatingSystems,
        Map<String, Long> deviceTypes,
        Map<String, Long> countries
) {}
