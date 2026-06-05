package br.com.lucas.shortlink.controllers;

import br.com.lucas.shortlink.dtos.response.ImageResponseDTO;
import br.com.lucas.shortlink.entities.Image;
import br.com.lucas.shortlink.services.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/v1/images")
@RequiredArgsConstructor
public class ImageControllerV1 {

    private final ImageService imageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponseDTO> uploadImage(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "tags", required = false) List<String> tags,
            Principal principal
    ) {
        String email = principal != null ? principal.getName() : null;

        Image savedImage = imageService.uploadImage(file, tags, email);

        ImageResponseDTO response = buildResponseDTO(savedImage);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<ImageResponseDTO> getImageDetails(@PathVariable String shortCode) {
        Image image = imageService.findByShortCode(shortCode);
        return ResponseEntity.ok(buildResponseDTO(image));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ImageResponseDTO>> searchImagesByTag(
            @RequestParam("tag") String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Image> imagePage = imageService.searchByTag(tag, pageable);

        Page<ImageResponseDTO> responsePage = imagePage.map(this::buildResponseDTO);

        return ResponseEntity.ok(responsePage);
    }

    @DeleteMapping("/{shortCode}")
    public ResponseEntity<Void> deleteImage(@PathVariable String shortCode, Principal principal) {
        String email = principal.getName();
        imageService.deleteImage(shortCode, email);
        return ResponseEntity.noContent().build();
    }

    // Método auxiliar para construir o DTO com as URLs dinâmicas baseadas no ambiente
    private ImageResponseDTO buildResponseDTO(Image image) {
        // Pega a URL base atual (ex: http://localhost:8080)
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        String shortUrl = baseUrl + "/i/" + image.getShortCode(); // Rota de visualização que você fará no Next.js
        String directFileUrl = baseUrl + "/uploads/" + image.getStorageUrl(); // Rota direta para a imagem física

        return new ImageResponseDTO(
                image.getOriginalFilename(),
                image.getShortCode(),
                shortUrl,
                directFileUrl,
                image.getTags(),
                image.getExpiresAt(),
                image.getUser() == null
        );
    }


    @GetMapping
    public ResponseEntity<Page<Image>> getUserImages(
            @RequestParam(defaultValue = "0") int page,
            Authentication authentication
    ) {
        String email = authentication.getName();

        Page<Image> imagesPage = imageService.getUserImages(email, page);

        return ResponseEntity.ok(imagesPage);
    }
}