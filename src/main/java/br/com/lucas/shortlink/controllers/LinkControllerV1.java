package br.com.lucas.shortlink.controllers;

import br.com.lucas.shortlink.dtos.request.LinkRequestDTO;
import br.com.lucas.shortlink.entities.Link;
import br.com.lucas.shortlink.services.LinkService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping
public class LinkControllerV1 {

    @Autowired
    private LinkService linkService;

    @PostMapping("/links")
    public ResponseEntity<Link> shorten(@Valid @RequestBody LinkRequestDTO linkRequestDTO, Authentication authentication){

        String email = authentication.getName();

        Link newLink = linkService.createLink(linkRequestDTO.originalUrl(), email);

        return  ResponseEntity.ok(newLink);
    }

    @GetMapping("/{shortCode}")
    public void redirect(@PathVariable String shortCode, HttpServletResponse response) throws IOException {
        String destination = linkService.getOriginalUrlByCode(shortCode);

        System.out.println("Eu sou o louco " + destination);

        if(destination != null){
            response.sendRedirect(destination);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Link não encontrado");
        }
    }
}
