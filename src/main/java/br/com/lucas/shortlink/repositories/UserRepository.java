package br.com.lucas.shortlink.repositories;

import br.com.lucas.shortlink.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    void deleteByEmailVerifiedFalseAndCreatedAtBefore(java.time.LocalDateTime time);
}
