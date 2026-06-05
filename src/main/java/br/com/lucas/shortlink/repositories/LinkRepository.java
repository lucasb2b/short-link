package br.com.lucas.shortlink.repositories;

import br.com.lucas.shortlink.entities.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

public interface LinkRepository extends JpaRepository<Link, UUID> {

    boolean existsByShortCode(String shortCode);

    Optional<Link> findByShortCode(String shortCode);

    Page<Link> findByUserEmail(String email, Pageable pageable);
}
