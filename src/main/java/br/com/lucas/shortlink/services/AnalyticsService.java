package br.com.lucas.shortlink.services;

import br.com.lucas.shortlink.config.RabbitMQConfig;
import br.com.lucas.shortlink.dtos.request.ClickEventDTO;
import br.com.lucas.shortlink.entities.Analytics;
import br.com.lucas.shortlink.entities.Link;
import br.com.lucas.shortlink.repositories.AnalyticsRepository;
import br.com.lucas.shortlink.repositories.LinkRepository;
import eu.bitwalker.useragentutils.UserAgent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AnalyticsService {

    @Autowired
    private AnalyticsRepository analyticsRepository;

    @Autowired
    private LinkRepository linkRepository; // Precisamos buscar o link para associar à entidade

    /**
     * O @RabbitListener faz esse método ficar escutando a fila.
     * Assim que o Controller enviar o DTO, isso executa em background.
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_ANALYTICS)
    @Transactional
    public void processClickEvent(ClickEventDTO event) {
        Optional<Link> linkOptional = linkRepository.findByShortCode(event.shortCode());

        if (linkOptional.isPresent()) {
            Link link = linkOptional.get();

            // Usa a biblioteca que você já implementou para fazer o parse
            UserAgent parsedUserAgent = UserAgent.parseUserAgentString(event.userAgent());

            Analytics analytics = Analytics.builder()
                    .link(link)
                    .ip(event.ipAddress())
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
        if (ip.equals("0:0:0:0:0:0:0:1") || ip.equals("127.0.0.1")) {
            return "Localhost";
        }
        return "Brazil"; // Futuramente você pode usar uma API de GeoIP aqui
    }
}