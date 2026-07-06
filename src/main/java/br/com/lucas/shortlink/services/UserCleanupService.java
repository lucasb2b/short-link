package br.com.lucas.shortlink.services;

import br.com.lucas.shortlink.repositories.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserCleanupService {

    private final UserRepository userRepository;

    public UserCleanupService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Executa a cada 5 minutos
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void deleteUnverifiedUsers() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(15);
        userRepository.deleteByEmailVerifiedFalseAndCreatedAtBefore(cutoff);
    }
}
