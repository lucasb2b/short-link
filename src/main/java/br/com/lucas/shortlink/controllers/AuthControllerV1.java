package br.com.lucas.shortlink.controllers;

import br.com.lucas.shortlink.dtos.request.LoginRequestDTO;
import br.com.lucas.shortlink.dtos.request.ResetPasswordRequestDTO;
import br.com.lucas.shortlink.dtos.response.LoginResponseDTO;
import br.com.lucas.shortlink.dtos.request.RegisterRequestDTO;
import br.com.lucas.shortlink.entities.EmailVerificationToken;
import br.com.lucas.shortlink.entities.PasswordResetToken;
import br.com.lucas.shortlink.entities.User;
import br.com.lucas.shortlink.repositories.EmailVerificationTokenRepository;
import br.com.lucas.shortlink.repositories.PasswordResetTokenRepository;
import br.com.lucas.shortlink.repositories.UserRepository;
import br.com.lucas.shortlink.security.JwtService;
import br.com.lucas.shortlink.services.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/v1/auth")
public class AuthControllerV1 {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final EmailVerificationTokenRepository tokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public AuthControllerV1(AuthenticationManager authenticationManager,
                            JwtService jwtService,
                            UserRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            EmailService emailService,
                            EmailVerificationTokenRepository emailVerificationToken,
                            PasswordResetTokenRepository passwordResetTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.tokenRepository = emailVerificationToken;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("E-mail já cadastrado");
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .emailVerified(false)
                .build();

        userRepository.save(user);

        String token = UUID.randomUUID().toString();

        EmailVerificationToken verificationToken =
                EmailVerificationToken.builder()
                        .token(token)
                        .user(user)
                        .expiresAt(LocalDateTime.now().plusHours(24))
                        .build();

        tokenRepository.save(verificationToken);

        String link = "http://localhost:8080/v1/auth/verify-email?token=" + token;


        emailService.sendEmail(
                user.getEmail(),
                "Confirme sua conta",
                "Clique no link:\n" + link

        );

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(
            @RequestParam String token
    ){
        EmailVerificationToken verificationToken = tokenRepository
                .findByToken(token)
                .orElse(null);

        if(verificationToken == null){
            return ResponseEntity.badRequest()
                    .body("Token inválido");
        }

        if(verificationToken.getExpiresAt()
                .isBefore(LocalDateTime.now())){

            return ResponseEntity.badRequest()
                    .body("Token expirado");

        }

        User user = verificationToken.getUser();

        user.setEmailVerified(true);

        userRepository.save(user);

        tokenRepository.delete(verificationToken);

        return ResponseEntity.ok("Email confirmado");

    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @RequestParam String email
    ) {

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken =
                PasswordResetToken.builder()
                        .token(token)
                        .user(user)
                        .expiresAt(LocalDateTime.now().plusMinutes(30))
                        .build();

        passwordResetTokenRepository.save(resetToken);

        String link =
                "http://localhost:8080/v1/auth/reset-password?token=" + token;

        emailService.sendEmail(
                user.getEmail(),
                "Recuperação de senha",
                "Clique no link:\n" + link
        );

        return ResponseEntity.ok("Email enviado");
    }

    @GetMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam String token
    ) {

        PasswordResetToken resetToken =
                passwordResetTokenRepository
                        .findByToken(token)
                        .orElse(null);

        if(resetToken == null) {
            return ResponseEntity.badRequest()
                    .body("Token inválido");
        }

        if(resetToken.getExpiresAt()
                .isBefore(LocalDateTime.now())){
            return  ResponseEntity.badRequest()
                    .body("Token expirado");
        }

        return ResponseEntity.ok("Token válido");

    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestBody ResetPasswordRequestDTO request
            ){
        PasswordResetToken resetToken =
                passwordResetTokenRepository.findByToken(request.token())
                        .orElse(null);

        if (resetToken == null){
            return ResponseEntity.badRequest()
                    .body("Token inválido");
        }

        if(resetToken.getExpiresAt()
                .isBefore(LocalDateTime.now())){
            return ResponseEntity.badRequest()
                    .body("Token expirado");
        }

        User user = resetToken.getUser();

        user.setPassword(
                passwordEncoder.encode(request.newPassword())
        );

        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);

        return ResponseEntity.ok("Senha alterada");
    }

    @GetMapping("/test-email")
    public String testEmail() {

        emailService.sendEmail(
                "po-py-copo@live.com",
                "You may sam I'm a dreamer",
                "But I am not the only one."
        );

        return "ok";
    }
}