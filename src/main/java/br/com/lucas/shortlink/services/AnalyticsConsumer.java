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

@Component
public class AnalyticsConsumer {

    private final AnalyticsRepository analyticsRepository;
    private final LinkRepository linkRepository;

    public AnalyticsConsumer(AnalyticsRepository analyticsRepository, LinkRepository linkRepository) {
        this.analyticsRepository = analyticsRepository;
        this.linkRepository = linkRepository;
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

    // Mantivemos o seu método de resolução de país
    private String resolveCountry(String ip) {
        if (ip.equals("0:0:0:0:0:0:0:1") || ip.equals("127.0.0.1")) {
            return "Localhost";
        }
        return "Brazil";
    }
}