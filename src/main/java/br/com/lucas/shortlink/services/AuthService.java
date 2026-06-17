package br.com.lucas.shortlink.services;

import br.com.lucas.shortlink.dtos.request.ChangePasswordRequestDTO;
import br.com.lucas.shortlink.dtos.request.LoginRequestDTO;
import br.com.lucas.shortlink.dtos.request.RegisterRequestDTO;
import br.com.lucas.shortlink.dtos.request.ResetPasswordRequestDTO;
import br.com.lucas.shortlink.entities.EmailVerificationToken;
import br.com.lucas.shortlink.entities.PasswordResetToken;
import br.com.lucas.shortlink.entities.User;
import br.com.lucas.shortlink.exceptions.*;
import br.com.lucas.shortlink.repositories.EmailVerificationTokenRepository;
import br.com.lucas.shortlink.repositories.PasswordResetTokenRepository;
import br.com.lucas.shortlink.repositories.UserRepository;
import br.com.lucas.shortlink.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    /**
     * Registra um novo usuário e envia e-mail de verificação.
     * @throws EmailAlreadyExistsException se o e-mail já estiver cadastrado
     */
    @Transactional
    public void register(RegisterRequestDTO request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new EmailAlreadyExistsException("E-mail já cadastrado");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .emailVerified(false)
                .build();
        userRepository.save(user);

        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
        emailVerificationTokenRepository.save(verificationToken);

        String link = "http://localhost:8080/v1/auth/verify-email?token=" + token;
        emailService.sendEmail(
                user.getEmail(),
                "Confirme sua conta",
                "Clique no link:\n" + link
        );
    }

    /**
     * Realiza login, verificando se o e-mail está confirmado.
     * @return token JWT
     * @throws InvalidCredentialsException se credenciais inválidas
     * @throws EmailNotVerifiedException se o e-mail não foi verificado
     */
    public String login(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciais inválidas"));
        if (!user.getEmailVerified()) {
            throw new EmailNotVerifiedException("E-mail não verificado. Confirme seu cadastro antes de fazer login.");
        }

        if (!user.getActive()) {
            throw new InactiveUserException("Conta desativada. Entre em contato com o suporte.");
        }

        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Credenciais inválidas");
        }

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("name", user.getName());

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return jwtService.generateToken(extraClaims, userDetails);
    }

    /**
     * Verifica um token de verificação de e-mail.
     * @throws TokenException se token inválido ou expirado
     */
    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new TokenException("Token inválido"));

        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenException("Token expirado");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        emailVerificationTokenRepository.delete(verificationToken);
    }

    /**
     * Envia e-mail de recuperação de senha, criando um token temporário.
     * @throws UserNotFoundException se o e-mail não estiver cadastrado
     */
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build();
        passwordResetTokenRepository.save(resetToken);

        String link = "http://localhost:8080/v1/auth/reset-password?token=" + token;
        emailService.sendEmail(
                user.getEmail(),
                "Recuperação de senha",
                "Clique no link:\n" + link
        );
    }

    /**
     * Valida se um token de reset de senha é válido (existe e não expirou).
     * @throws TokenException se inválido ou expirado
     */
    public void validateResetToken(String token) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new TokenException("Token inválido"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenException("Token expirado");
        }
    }

    /**
     * Efetiva a troca de senha usando o token e a nova senha.
     * @throws TokenException se token inválido/expirado
     */
    @Transactional
    public void resetPassword(ResetPasswordRequestDTO request) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByToken(request.token())
                .orElseThrow(() -> new TokenException("Token inválido"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenException("Token expirado");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);
    }

    @Transactional
    public void deactivateAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));
        user.setActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void reactivateAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));
        user.setActive(true);
        userRepository.save(user);
    }

    /**
     * Altera a senha de um usuário autenticado.
     * @throws UserNotFoundException se o usuário não for encontrado
     * @throws InvalidCredentialsException se a senha atual estiver incorreta
     */
    @Transactional
    public void changePassword(String email, ChangePasswordRequestDTO request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("A senha atual está incorreta.");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("A nova senha não pode ser igual à senha atual.");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));

        userRepository.save(user);
    }

}