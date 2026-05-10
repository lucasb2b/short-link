package br.com.lucas.shortlink.services;

import br.com.lucas.shortlink.entities.Analytics;
import br.com.lucas.shortlink.entities.Link;
import br.com.lucas.shortlink.repositories.AnalyticsRepository;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {

    @Autowired
    private AnalyticsRepository analyticsRepository;

    public void recordClick(Link link, HttpServletRequest request){
        String uaString = request.getHeader("User-Agent");
        String ipAddress = getClientIp(request);

        UserAgent userAgent = UserAgent.parseUserAgentString(uaString);

        Analytics analytics = Analytics.builder()
                .link(link)
                .ip(ipAddress)
                .userAgent(uaString)
                .browser(userAgent.getBrowser().getName())
                .operatingSystem(userAgent.getOperatingSystem().getName())
                .deviceType(userAgent.getOperatingSystem().getDeviceType().getName())
                .country(resolveCountry(ipAddress))
                .build();

        analyticsRepository.save(analytics);
    }

    private String resolveCountry(String ip) {
        if (ip.equals("0:0:0:0:0:0:0:1") || ip.equals("127.0.0.1")) {
            return "Localhost";
        }

        return "Brazil";
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isEmpty()){
            return xf.split(",")[0];
        }
        return request.getRemoteAddr();
    }

}
