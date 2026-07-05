package br.com.lucas.shortlink.controllers;

import br.com.lucas.shortlink.dtos.response.StatsResponseDTO;
import br.com.lucas.shortlink.services.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/stats")
@RequiredArgsConstructor
public class StatsControllerV1 {

    private final StatsService statsService;

    @GetMapping("/overview")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Obter estatísticas globais", description = "Retorna um resumo de todas as estatísticas consolidadas do usuário.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estatísticas recuperadas com sucesso")
    })
    public ResponseEntity<StatsResponseDTO> getOverview(Authentication authentication) {
        String email = authentication.getName();
        StatsResponseDTO response = statsService.getUserStats(email);
        return ResponseEntity.ok(response);
    }
}
