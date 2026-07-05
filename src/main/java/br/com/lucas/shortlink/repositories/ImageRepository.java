package br.com.lucas.shortlink.repositories;

import br.com.lucas.shortlink.entities.Image;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ImageRepository extends JpaRepository<Image, UUID> {

    Optional<Image> findByShortCode(String shortCode);

    // Busca imagens que contenham a tag especificada, ignorando letras maiúsculas/minúsculas
    @Query("SELECT i FROM Image i JOIN i.tags t WHERE LOWER(t) = LOWER(:tag)")
    Page<Image> findByTag(@Param("tag") String tag, Pageable pageable);

    // Método útil para criar um @Scheduled no futuro para apagar do banco os expirados
    List<Image> findAllByExpiresAtBefore(LocalDateTime dateTime);

    Page<Image> findByUserEmail(String email, Pageable pageable);

    long countByUserEmail(String email);
}