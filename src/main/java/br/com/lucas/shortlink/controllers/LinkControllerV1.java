package br.com.lucas.shortlink.controllers;

import br.com.lucas.shortlink.dtos.request.ClickEventDTO;
import br.com.lucas.shortlink.dtos.request.LinkRequestDTO;
import br.com.lucas.shortlink.dtos.response.AnalyticsResponseDTO;
import br.com.lucas.shortlink.dtos.response.LinkResponseDTO;
import br.com.lucas.shortlink.entities.Analytics;
import br.com.lucas.shortlink.entities.Link;
import br.com.lucas.shortlink.services.AnalyticsProducer;
import br.com.lucas.shortlink.repositories.AnalyticsRepository;
import br.com.lucas.shortlink.services.LinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping
@Tag(name = "Links", description = "Encurtamento, redirecionamento e gerenciamento de links")
public class LinkControllerV1 {

    private final LinkService linkService;
    private final AnalyticsProducer analyticsProducer; // Substituímos o AnalyticsService pelo Producer
    private final AnalyticsRepository analyticsRepository;

    public LinkControllerV1(
            LinkService linkService,
            AnalyticsProducer analyticsProducer,
            AnalyticsRepository analyticsRepository
    ){
        this.linkService = linkService;
        this.analyticsProducer = analyticsProducer;
        this.analyticsRepository = analyticsRepository;
    }

    @PostMapping("/links")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Encurtar URL", description = "Cria um link curto para a URL fornecida. Requer autenticação.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Link encurtado com sucesso",
                    content = @Content(schema = @Schema(implementation = LinkResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "URL inválida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<LinkResponseDTO> shorten(
            @Valid @RequestBody LinkRequestDTO linkRequestDTO,
            Authentication authentication){
        String email = authentication.getName();

        Link newLink = linkService.createShortLink(linkRequestDTO.originalUrl(), email);

        LinkResponseDTO response = new LinkResponseDTO(
                newLink.getOriginalUrl(),
                newLink.getShortUrl(),
                newLink.getShortCode()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/links/{shortCode}/analytics")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Obter Analytics do Link", description = "Retorna os dados estatísticos detalhados de cliques do link. Requer autenticação.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados de analytics retornados com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Link não encontrado")
    })
    public ResponseEntity<AnalyticsResponseDTO> getLinkAnalytics(
            @Parameter(description = "Código curto do link", required = true)
            @PathVariable String shortCode,
            Authentication authentication
    ) {
        String email = authentication.getName();
        // Valida se o link existe e pertence ao usuário (reutilizando a lógica do service)
        Link link = linkService.findByShortCode(shortCode);
        if (!link.getUser().getEmail().equals(email)) {
            return ResponseEntity.status(403).build(); // 403 Forbidden
        }
        // Busca todas as métricas desse link
        List<Analytics> analyticsList = analyticsRepository.findAllByLink(link);
        long totalClicks = analyticsList.size();
        // Agrupa pela quantidade de ocorrências
        Map<String, Long> browsers = analyticsList.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getBrowser() != null ? a.getBrowser() : "Outros",
                        Collectors.counting()
                ));
        Map<String, Long> operatingSystems = analyticsList.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getOperatingSystem() != null ? a.getOperatingSystem() : "Outros",
                        Collectors.counting()
                ));
        Map<String, Long> deviceTypes = analyticsList.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getDeviceType() != null ? a.getDeviceType() : "Outros",
                        Collectors.counting()
                ));
        Map<String, Long> countries = analyticsList.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getCountry() != null ? a.getCountry() : "Outros",
                        Collectors.counting()
                ));
        // Monta o DTO final e retorna
        AnalyticsResponseDTO responseDTO = new AnalyticsResponseDTO(
                totalClicks,
                browsers,
                operatingSystems,
                deviceTypes,
                countries
        );
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{shortCode}")
    @Operation(summary = "Redirecionar para URL original",
            description = "Acessa um link curto, registra o clique e redireciona para a URL original.")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Redirecionamento para a URL original"),
            @ApiResponse(responseCode = "404", description = "Link não encontrado")
    })
    public ResponseEntity<Void> redirect(
            @Parameter(description = "Código curto do link", required = true)
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

    @GetMapping("/links/{shortCode}")
    @Operation(summary = "Obter detalhes do link", description = "Retorna os dados do link encurtado sem redirecionar.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalhes do link"),
            @ApiResponse(responseCode = "404", description = "Link não encontrado")
    })
    public ResponseEntity<LinkResponseDTO> getLinkInfo(
            @Parameter(description = "Código curto do link", required = true)
            @PathVariable String shortCode
    ) {
        Link link = linkService.findByShortCode(shortCode); // usa o mesmo método que já valida revogação
        LinkResponseDTO response = new LinkResponseDTO(
                link.getOriginalUrl(),
                link.getShortUrl(),
                link.getShortCode()
        );
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/links/{shortCode}/revoke")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Revogar link", description = "Revoga um link encurtado, impedindo novos acessos. Requer autenticação.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Link revogado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Não autorizado a revogar este link"),
            @ApiResponse(responseCode = "404", description = "Link não encontrado")
    })
    public ResponseEntity<Void> revokeLink(
            @Parameter(description = "Código curto do link a ser revogado", required = true)
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
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Listar links do usuário", description = "Retorna uma página com os links encurtados pelo usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de links do usuário")
    })
    public ResponseEntity<Page<LinkResponseDTO>> getUserLinks(
            @Parameter(description = "Número da página (zero-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            Authentication authentication
    ) {
        String email = authentication.getName();

        // Busca os links paginados
        Page<Link> linksPage = linkService.getUserLinks(email, page);

        // Converte a Page de Entidades para uma Page de DTOs para não vazar dados sensíveis
        Page<LinkResponseDTO> responsePage = linksPage.map(link -> new LinkResponseDTO(
                link.getOriginalUrl(),
                link.getShortUrl(),
                link.getShortCode()
        ));

        return ResponseEntity.ok(responsePage);
    }
}