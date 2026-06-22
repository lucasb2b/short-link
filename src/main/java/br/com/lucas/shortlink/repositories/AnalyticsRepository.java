package br.com.lucas.shortlink.repositories;

import br.com.lucas.shortlink.entities.Analytics;
import br.com.lucas.shortlink.entities.Link;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AnalyticsRepository extends JpaRepository<Analytics, UUID> {
    List<Analytics> findAllByLink(Link link);
}
