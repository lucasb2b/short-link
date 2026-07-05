package br.com.lucas.shortlink.repositories;

import br.com.lucas.shortlink.entities.Analytics;
import br.com.lucas.shortlink.entities.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AnalyticsRepository extends JpaRepository<Analytics, UUID> {
    List<Analytics> findAllByLink(Link link);

    @Query("SELECT COUNT(a) FROM Analytics a WHERE a.link.user.email = :email")
    long countTotalClicksByUser(@Param("email") String email);

    @Query("SELECT a.country, COUNT(a) FROM Analytics a WHERE a.link.user.email = :email GROUP BY a.country ORDER BY COUNT(a) DESC")
    List<Object[]> countClicksByCountry(@Param("email") String email);

    @Query("SELECT a.browser, COUNT(a) FROM Analytics a WHERE a.link.user.email = :email GROUP BY a.browser ORDER BY COUNT(a) DESC")
    List<Object[]> countClicksByBrowser(@Param("email") String email);

    @Query("SELECT a.operatingSystem, COUNT(a) FROM Analytics a WHERE a.link.user.email = :email GROUP BY a.operatingSystem ORDER BY COUNT(a) DESC")
    List<Object[]> countClicksByOperatingSystem(@Param("email") String email);
}
