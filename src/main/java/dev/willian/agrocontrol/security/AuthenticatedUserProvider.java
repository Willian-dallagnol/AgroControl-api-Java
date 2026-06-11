package dev.willian.agrocontrol.security;

import dev.willian.agrocontrol.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Fornece a entidade User autenticada a partir do SecurityContext.
 * Usado pelos servicos para escopar dados ao dono.
 */
@Component
public class AuthenticatedUserProvider {

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser securityUser)) {
            throw new UsernameNotFoundException("Nenhum usuario autenticado no contexto.");
        }
        return securityUser.getDomainUser();
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
