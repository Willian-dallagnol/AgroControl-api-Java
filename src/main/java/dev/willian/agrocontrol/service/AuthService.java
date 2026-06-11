package dev.willian.agrocontrol.service;

import dev.willian.agrocontrol.domain.Role;
import dev.willian.agrocontrol.domain.User;
import dev.willian.agrocontrol.dto.UserResponse;
import dev.willian.agrocontrol.dto.auth.AuthResponse;
import dev.willian.agrocontrol.dto.auth.LoginRequest;
import dev.willian.agrocontrol.dto.auth.RegisterRequest;
import dev.willian.agrocontrol.exception.DuplicateResourceException;
import dev.willian.agrocontrol.mapper.UserMapper;
import dev.willian.agrocontrol.repository.UserRepository;
import dev.willian.agrocontrol.security.AuthenticatedUserProvider;
import dev.willian.agrocontrol.security.JwtService;
import dev.willian.agrocontrol.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final AuthenticatedUserProvider currentUser;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Tentativa de registro com e-mail ja existente: {}", request.email());
            throw new DuplicateResourceException("E-mail ja cadastrado: " + request.email());
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role() != null ? request.role() : Role.OPERATOR)
                .active(true)
                .build();

        User saved = userRepository.save(user);
        log.info("Novo usuario registrado. id={}, role={}", saved.getId(), saved.getRole());
        String token = jwtService.generateToken(saved);
        return AuthResponse.bearer(token, jwtService.getExpirationMs(), userMapper.toResponse(saved));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = ((SecurityUser) authentication.getPrincipal()).getDomainUser();
        log.info("Login bem-sucedido. userId={}", user.getId());
        String token = jwtService.generateToken(user);
        return AuthResponse.bearer(token, jwtService.getExpirationMs(), userMapper.toResponse(user));
    }

    /**
     * Retorna o usuario dono do token atual. Util para o frontend exibir/validar a sessao.
     */
    @Transactional(readOnly = true)
    public UserResponse me() {
        return userMapper.toResponse(currentUser.getCurrentUser());
    }
}
