package dev.willian.agrocontrol.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Rate limiting simples (janela fixa, em memoria) para os endpoints de autenticacao.
 * Limita tentativas de login/registro por IP, mitigando brute force e abuso.
 *
 * <p>Limpeza automatica: a cada {@link #CLEANUP_INTERVAL_SECONDS} o mapa e varrido e as
 * janelas ja expiradas sao removidas, evitando crescimento indefinido (memory leak).
 *
 * <p>Observacao para producao: esta implementacao e por instancia (nao distribuida).
 * Em um cluster, usar Bucket4j + Redis ou um API Gateway. Aqui, o objetivo e demonstrar
 * o conceito sem adicionar dependencias ou infraestrutura desnecessarias ao portfolio.
 */
@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String AUTH_PATH_PREFIX = "/api/v1/auth/";
    static final int MAX_REQUESTS_PER_WINDOW = 10;
    static final long WINDOW_SECONDS = 60;
    static final long CLEANUP_INTERVAL_SECONDS = 300; // varre o mapa a cada 5 min

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final AtomicLong lastCleanupEpoch = new AtomicLong(0);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // So aplica rate limit nos endpoints de autenticacao.
        return !request.getRequestURI().startsWith(AUTH_PATH_PREFIX);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String clientId = resolveClientIp(request);
        if (isLimitExceeded(clientId, Instant.now().getEpochSecond())) {
            log.warn("Rate limit excedido. ip={}, path={}", clientId, request.getRequestURI());
            writeTooManyRequests(response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Conta a requisicao na janela atual do cliente e diz se o limite foi ultrapassado.
     * Package-private para permitir testes determinísticos com o tempo controlado.
     */
    boolean isLimitExceeded(String clientId, long nowEpoch) {
        evictExpiredIfDue(nowEpoch);
        Window window = windows.compute(clientId, (key, current) -> {
            if (current == null || nowEpoch - current.startEpoch >= WINDOW_SECONDS) {
                return new Window(nowEpoch); // nova janela
            }
            return current;
        });
        return window.count.incrementAndGet() > MAX_REQUESTS_PER_WINDOW;
    }

    /**
     * Remove janelas expiradas, mas no maximo uma vez a cada CLEANUP_INTERVAL_SECONDS,
     * para nao varrer o mapa inteiro a cada requisicao. Apenas uma thread executa a varredura.
     */
    void evictExpiredIfDue(long nowEpoch) {
        long last = lastCleanupEpoch.get();
        if (nowEpoch - last < CLEANUP_INTERVAL_SECONDS) {
            return;
        }
        if (!lastCleanupEpoch.compareAndSet(last, nowEpoch)) {
            return; // outra thread ja assumiu a limpeza
        }
        windows.entrySet().removeIf(entry -> nowEpoch - entry.getValue().startEpoch >= WINDOW_SECONDS);
    }

    /** Quantidade de clientes rastreados no momento. Usado em testes. */
    int trackedClients() {
        return windows.size();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim(); // primeiro IP da cadeia de proxies
        }
        return request.getRemoteAddr();
    }

    private void writeTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                "{\"status\":429,\"error\":\"Too Many Requests\","
                        + "\"message\":\"Limite de requisicoes excedido. Tente novamente em instantes.\"}");
    }

    /** Janela de contagem: instante de inicio + contador atomico de requisicoes. */
    private static final class Window {
        private final long startEpoch;
        private final AtomicInteger count = new AtomicInteger(0);

        private Window(long startEpoch) {
            this.startEpoch = startEpoch;
        }
    }
}
