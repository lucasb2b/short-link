package br.com.lucas.shortlink.repositories;

import br.com.lucas.shortlink.entities.Analytics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AnalyticsRepository extends JpaRepository<Analytics, UUID> {
}
