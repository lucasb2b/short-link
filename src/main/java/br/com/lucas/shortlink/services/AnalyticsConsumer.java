package br.com.lucas.shortlink.services;

import br.com.lucas.shortlink.config.RabbitMQConfig;
import br.com.lucas.shortlink.dtos.request.ClickEventDTO;
import br.com.lucas.shortlink.entities.Analytics;
import br.com.lucas.shortlink.entities.Link;
import br.com.lucas.shortlink.repositories.AnalyticsRepository;
import br.com.lucas.shortlink.repositories.LinkRepository;
import eu.bitwalker.useragentutils.UserAgent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import lombok.Data;

@Component
public class AnalyticsConsumer {

    private final AnalyticsRepository analyticsRepository;
    private final LinkRepository linkRepository;
    private final RestTemplate restTemplate;

    public AnalyticsConsumer(AnalyticsRepository analyticsRepository, LinkRepository linkRepository) {
        this.analyticsRepository = analyticsRepository;
        this.linkRepository = linkRepository;
        this.restTemplate = new RestTemplate();
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ANALYTICS)
    @Transactional
    public void processClickEvent(ClickEventDTO event) {

        Link link = linkRepository.findByShortCode(event.shortCode())
                .orElse(null);

        if (link != null) {
            // Usa a biblioteca bitwalker para extrair os dados do dispositivo
            UserAgent parsedUserAgent = UserAgent.parseUserAgentString(event.userAgent());

            Analytics analytics = Analytics.builder()
                    .link(link)
                    .ip(event.ipAddress()) // <-- Correção: Alterado de .ipAddress para .ip
                    .userAgent(event.userAgent())
                    .browser(parsedUserAgent.getBrowser().getName())
                    .operatingSystem(parsedUserAgent.getOperatingSystem().getName())
                    .deviceType(parsedUserAgent.getOperatingSystem().getDeviceType().getName())
                    .country(resolveCountry(event.ipAddress()))
                    .build();

            analyticsRepository.save(analytics);
        }
    }

    private String resolveCountry(String ip) {
        if (ip == null || ip.equals("0:0:0:0:0:0:0:1") || ip.equals("127.0.0.1") || ip.startsWith("192.168.")) {
            return "Localhost";
        }
        try {
            IpApiResponse response = restTemplate.getForObject("http://ip-api.com/json/" + ip, IpApiResponse.class);
            if (response != null && "success".equals(response.getStatus()) && response.getCountry() != null) {
                return response.getCountry();
            }
        } catch (Exception e) {
            // Em caso de falha de conexão com a API, ignora e retorna Desconhecido
        }
        return "Desconhecido";
    }

    @Data
    public static class IpApiResponse {
        private String status;
        private String country;
    }
}