package br.com.lucas.shortlink.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Test", description = "Endpoint de teste de autenticação")
public class TestController {

    @GetMapping("/test")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Testar autenticação", description = "Retorna uma mensagem de confirmação se o usuário estiver autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário autenticado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public String test(){
        return "Você está autenticado!";
    }
}