package br.com.lucas.shortlink.controllers;

import br.com.lucas.shortlink.dtos.request.LinkRequestDTO;
import br.com.lucas.shortlink.dtos.response.LinkResponseDTO;
import br.com.lucas.shortlink.entities.Link;
import br.com.lucas.shortlink.services.AnalyticsService;
import br.com.lucas.shortlink.services.LinkService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class LinkControllerV1 {

    private final LinkService linkService;
    private final AnalyticsService analyticsService;

    public LinkControllerV1(
            LinkService linkService,
            AnalyticsService analyticsService
    ){
        this.linkService = linkService;
        this.analyticsService = analyticsService;
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
        Link link = linkService.findByShortCode(shortCode);

        analyticsService.recordClick(link, request);

        return ResponseEntity
                .status(302)
                .header("Location", link.getOriginalUrl())
                .build();
    }
}
