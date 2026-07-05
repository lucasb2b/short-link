package br.com.lucas.shortlink.services;

import br.com.lucas.shortlink.entities.Image;
import br.com.lucas.shortlink.entities.User;
import br.com.lucas.shortlink.exceptions.FileStorageException;
import br.com.lucas.shortlink.exceptions.InvalidFileException;
import br.com.lucas.shortlink.exceptions.ResourceNotFoundException;
import br.com.lucas.shortlink.repositories.ImageRepository;
import br.com.lucas.shortlink.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final ImageRepository imageRepository;
    private final UserRepository userRepository;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/gif", "image/webp");

    // Define a pasta local onde os arquivos serão salvos
    private final Path uploadDir = Paths.get("uploads");

    // Executa assim que a aplicação sobe para garantir que a pasta "uploads" existe
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new FileStorageException("Não foi possível criar a pasta de uploads.");
        }
    }

    @Transactional
    public Image uploadImage(MultipartFile file, List<String> tags, String email) {
        validateFile(file);

        User user = null;
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);

        if (email != null) {
            user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
            expiresAt = null;
        }

        String shortCode = generateUniqueShortCode();

        // Salva o arquivo fisicamente no computador e retorna o nome gerado
        String fileName = saveFileLocally(file, shortCode);

        Image image = Image.builder()
                .originalFilename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .storageUrl(fileName) // Guardamos apenas o nome do arquivo no banco (ex: a1b2c3_foto.png)
                .shortCode(shortCode)
                .expiresAt(expiresAt)
                .user(user)
                .tags(tags != null ? tags : List.of())
                .build();

        return imageRepository.save(image);
    }

    public Image findByShortCode(String shortCode) {
        Image image = imageRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("Imagem não encontrada"));

        if (image.getExpiresAt() != null && image.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResourceNotFoundException("Esta imagem expirou e não está mais disponível.");
        }

        return image;
    }

    public Page<Image> searchByTag(String tag, Pageable pageable) {
        return imageRepository.findByTag(tag, pageable);
    }

    @Transactional
    public void deleteImage(String shortCode, String email) {
        Image image = findByShortCode(shortCode);

        if (image.getUser() != null) {
            if (email == null || !image.getUser().getEmail().equals(email)) {
                throw new org.springframework.security.access.AccessDeniedException("Você não tem permissão para deletar esta imagem.");
            }
        } else {
            throw new org.springframework.security.access.AccessDeniedException("Imagens anônimas não podem ser deletadas manualmente, elas expiram sozinhas em 30 dias.");
        }

        // Apaga o arquivo físico do disco antes de apagar do banco de dados
        try {
            Path filePath = uploadDir.resolve(image.getStorageUrl());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new FileStorageException("Erro ao deletar o arquivo físico da imagem.");
        }

        imageRepository.delete(image);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidFileException("O arquivo está vazio.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileException("O arquivo excede o limite de 5MB.");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new InvalidFileException("Tipo de arquivo não suportado. Apenas JPEG, PNG, GIF e WEBP.");
        }
    }

    // Método que faz a gravação real no HD
    private String saveFileLocally(MultipartFile file, String shortCode) {
        try {
            // Pega a extensão original (ex: .png)
            String originalName = file.getOriginalFilename();
            String extension = originalName != null && originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : "";

            // Gera um nome único para evitar conflito (ex: xyz123.png)
            String fileName = shortCode + extension;

            // Resolve o caminho final (uploads/xyz123.png) e copia os bytes
            Path targetLocation = this.uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Não foi possível armazenar o arquivo. Tente novamente!");
        }
    }

    private String generateUniqueShortCode() {
        return UUID.randomUUID().toString().substring(0, 6);
    }

    public Page<Image> getUserImages(String email, int page) {
        // Cria a paginação: página atual, tamanho 10, ordenado do mais recente para o mais antigo
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        return imageRepository.findByUserEmail(email, pageable);
    }

    // Roda todo dia à meia noite
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void cleanupExpiredImages() {
        log.info("Iniciando rotina de limpeza de imagens expiradas...");
        List<Image> expiredImages = imageRepository.findAllByExpiresAtBefore(LocalDateTime.now());
        
        int deletedCount = 0;
        for (Image image : expiredImages) {
            try {
                Path filePath = uploadDir.resolve(image.getStorageUrl());
                Files.deleteIfExists(filePath);
                imageRepository.delete(image);
                deletedCount++;
            } catch (IOException e) {
                log.error("Erro ao deletar o arquivo físico da imagem expirada: {}", image.getStorageUrl(), e);
            }
        }
        log.info("Limpeza finalizada. {} imagens anônimas deletadas.", deletedCount);
    }
}