package br.com.lucas.shortlink.controllers;

import br.com.lucas.shortlink.dtos.response.ImageResponseDTO;
import br.com.lucas.shortlink.entities.Image;
import br.com.lucas.shortlink.services.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/v1/images")
@RequiredArgsConstructor
@Tag(name = "Images", description = "Upload e consulta de imagens encurtadas")
public class ImageControllerV1 {

    private final ImageService imageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Fazer upload de imagem",
            description = "Envia um arquivo de imagem com tags opcionais. Pode ser usado de forma anônima ou autenticada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Upload realizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ImageResponseDTO.class)))
    })
    public ResponseEntity<ImageResponseDTO> uploadImage(
            @Parameter(description = "Arquivo de imagem a ser enviado", required = true)
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "Lista de tags (opcional)", required = false)
            @RequestParam(value = "tags", required = false) List<String> tags,
            @Parameter(description = "Define se a imagem é privada", required = false)
            @RequestParam(value = "isPrivate", required = false, defaultValue = "false") boolean isPrivate,
            Principal principal
    ) {
        String email = principal != null ? principal.getName() : null;

        Image savedImage = imageService.uploadImage(file, tags, email, isPrivate);

        ImageResponseDTO response = buildResponseDTO(savedImage);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{shortCode}")
    @Operation(summary = "Obter detalhes da imagem", description = "Retorna as informações da imagem a partir do código curto.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalhes da imagem",
                    content = @Content(schema = @Schema(implementation = ImageResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Imagem não encontrada")
    })
    public ResponseEntity<ImageResponseDTO> getImageDetails(
            @Parameter(description = "Código curto da imagem", required = true)
            @PathVariable String shortCode,
            Principal principal) {
        String email = principal != null ? principal.getName() : null;
        Image image = imageService.findByShortCode(shortCode, email);
        return ResponseEntity.ok(buildResponseDTO(image));
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar imagens por tag", description = "Retorna uma página de imagens filtradas pela tag fornecida.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de imagens correspondentes")
    })
    public ResponseEntity<Page<ImageResponseDTO>> searchImagesByTag(
            @Parameter(description = "Tag para filtrar", required = true)
            @RequestParam("tag") String tag,
            @Parameter(description = "Número da página (zero-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Quantidade de itens por página", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Image> imagePage = imageService.searchByTag(tag, pageable);

        Page<ImageResponseDTO> responsePage = imagePage.map(this::buildResponseDTO);

        return ResponseEntity.ok(responsePage);
    }

    @DeleteMapping("/{shortCode}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Excluir imagem", description = "Remove a imagem associada ao código curto. Requer autenticação.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Imagem excluída com sucesso"),
            @ApiResponse(responseCode = "403", description = "Não autorizado a excluir esta imagem"),
            @ApiResponse(responseCode = "404", description = "Imagem não encontrada")
    })
    public ResponseEntity<Void> deleteImage(
            @Parameter(description = "Código curto da imagem", required = true)
            @PathVariable String shortCode,
            Principal principal) {
        String email = principal.getName();
        imageService.deleteImage(shortCode, email);
        return ResponseEntity.noContent().build();
    }

    // Método auxiliar para construir o DTO com as URLs dinâmicas baseadas no ambiente
    private ImageResponseDTO buildResponseDTO(Image image) {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        String shortUrl = baseUrl + "/i/" + image.getShortCode();
        String directFileUrl = baseUrl + "/uploads/" + image.getStorageUrl();

        return new ImageResponseDTO(
                image.getOriginalFilename(),
                image.getShortCode(),
                shortUrl,
                directFileUrl,
                image.getTags(),
                image.getExpiresAt(),
                image.getUser() == null,
                image.isPrivate(),
                image.getFileSize(),
                image.getCreatedAt()
        );
    }

    @PatchMapping("/{shortCode}/visibility")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Alternar visibilidade", description = "Alterna entre público e privado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Visibilidade alterada",
                    content = @Content(schema = @Schema(implementation = ImageResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Sem permissão"),
            @ApiResponse(responseCode = "404", description = "Imagem não encontrada")
    })
    public ResponseEntity<ImageResponseDTO> toggleVisibility(
            @Parameter(description = "Código curto da imagem", required = true)
            @PathVariable String shortCode,
            Principal principal) {
        String email = principal.getName();
        Image updated = imageService.toggleVisibility(shortCode, email);
        return ResponseEntity.ok(buildResponseDTO(updated));
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Listar imagens do usuário", description = "Retorna uma página com as imagens enviadas pelo usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de imagens do usuário")
    })
    public ResponseEntity<Page<ImageResponseDTO>> getUserImages(
            @Parameter(description = "Número da página (zero-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            Authentication authentication
    ) {
        String email = authentication.getName();

        Page<Image> imagesPage = imageService.getUserImages(email, page);
        Page<ImageResponseDTO> responsePage = imagesPage.map(this::buildResponseDTO);

        return ResponseEntity.ok(responsePage);
    }
}