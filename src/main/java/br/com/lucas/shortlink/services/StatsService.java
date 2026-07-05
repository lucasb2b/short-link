package br.com.lucas.shortlink.services;

import br.com.lucas.shortlink.dtos.response.StatsResponseDTO;
import br.com.lucas.shortlink.repositories.AnalyticsRepository;
import br.com.lucas.shortlink.repositories.ImageRepository;
import br.com.lucas.shortlink.repositories.LinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final LinkRepository linkRepository;
    private final ImageRepository imageRepository;
    private final AnalyticsRepository analyticsRepository;

    private static final long BYTES_SAVED_PER_CLICK = 130L;

    public StatsResponseDTO getUserStats(String email) {
        long totalLinks = linkRepository.countByUserEmail(email);
        long totalPhotos = imageRepository.countByUserEmail(email);
        long totalClicks = analyticsRepository.countTotalClicksByUser(email);
        
        long trafficSavedBytes = totalClicks * BYTES_SAVED_PER_CLICK;

        Map<String, Long> topCountries = formatToMap(analyticsRepository.countClicksByCountry(email));
        Map<String, Long> topBrowsers = formatToMap(analyticsRepository.countClicksByBrowser(email));
        Map<String, Long> topOS = formatToMap(analyticsRepository.countClicksByOperatingSystem(email));

        return new StatsResponseDTO(
                totalLinks,
                totalClicks,
                totalPhotos,
                trafficSavedBytes,
                topCountries,
                topBrowsers,
                topOS
        );
    }

    private Map<String, Long> formatToMap(List<Object[]> queryResult) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : queryResult) {
            String key = row[0] != null ? row[0].toString() : "Outros";
            Long value = ((Number) row[1]).longValue();
            map.put(key, map.getOrDefault(key, 0L) + value);
        }
        return map;
    }
}
