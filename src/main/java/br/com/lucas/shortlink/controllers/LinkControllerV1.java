package br.com.lucas.shortlink.controllers;

import br.com.lucas.shortlink.dtos.request.ClickEventDTO;
import br.com.lucas.shortlink.dtos.request.LinkRequestDTO;
import br.com.lucas.shortlink.dtos.response.LinkResponseDTO;
import br.com.lucas.shortlink.entities.Link;
import br.com.lucas.shortlink.services.AnalyticsProducer;
import br.com.lucas.shortlink.services.LinkService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping
public class LinkControllerV1 {

    private final LinkService linkService;
    private final AnalyticsProducer analyticsProducer; // Substituímos o AnalyticsService pelo Producer

    public LinkControllerV1(
            LinkService linkService,
            AnalyticsProducer analyticsProducer
    ){
        this.linkService = linkService;
        this.analyticsProducer = analyticsProducer;
    }

    @PostMapping("/links")
    public ResponseEntity<LinkResponseDTO> shorten(@Valid @RequestBody LinkRequestDTO linkRequestDTO, Authentication authentication){
        String email = authentication.getName();

        Link newLink = linkService.createShortLink(linkRequestDTO.originalUrl(), email);

        LinkResponseDTO response = new LinkResponseDTO(
                newLink.getOriginalUrl(),
                newLink.getShortCode(),
                newLink.getShortUrl()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode,
            HttpServletRequest request
    ){
        // Busca do Redis/Postgres
        Link link = linkService.findByShortCode(shortCode);

        // Extrai os dados do request antes dele "morrer"
        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        // Monta o evento e joga na fila do RabbitMQ
        ClickEventDTO clickEvent = new ClickEventDTO(shortCode, ipAddress, userAgent);
        analyticsProducer.sendClickEvent(clickEvent);

        // Redireciona imediatamente
        return ResponseEntity
                .status(302)
                .header("Location", link.getOriginalUrl())
                .build();
    }

    @PatchMapping("/links/{shortCode}/revoke")
    public ResponseEntity<Void> revokeLink(
            @PathVariable String shortCode,
            Authentication authentication
    ){
        String email = authentication.getName();
        linkService.revokeLink(shortCode, email);
        return ResponseEntity.noContent().build();
    }

    // Método extraído do service para o controller
    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isEmpty()){
            return xf.split(",")[0];
        }
        return request.getRemoteAddr();
    }

    @GetMapping("/links")
    public ResponseEntity<Page<LinkResponseDTO>> getUserLinks(
            @RequestParam(defaultValue = "0") int page, // Pega o param ?page=0 da URL
            Authentication authentication
    ) {
        String email = authentication.getName();

        // Busca os links paginados
        Page<Link> linksPage = linkService.getUserLinks(email, page);

        // Converte a Page de Entidades para uma Page de DTOs para não vazar dados sensíveis
        Page<LinkResponseDTO> responsePage = linksPage.map(link -> new LinkResponseDTO(
                link.getOriginalUrl(),
                link.getShortCode(),
                link.getShortUrl()
        ));

        return ResponseEntity.ok(responsePage);
    }
}