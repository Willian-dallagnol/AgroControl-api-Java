package dev.willian.agrocontrol.security;

import org.junit.jupiter.api.Test;

import static dev.willian.agrocontrol.security.RateLimitFilter.CLEANUP_INTERVAL_SECONDS;
import static dev.willian.agrocontrol.security.RateLimitFilter.MAX_REQUESTS_PER_WINDOW;
import static dev.willian.agrocontrol.security.RateLimitFilter.WINDOW_SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes deterministicos do rate limiter. Como isLimitExceeded recebe o "agora" como
 * parametro, conseguimos simular a passagem do tempo sem Thread.sleep nem relogio real.
 */
class RateLimitFilterTest {

    private final RateLimitFilter filter = new RateLimitFilter();

    @Test
    void allowsRequestsUpToTheLimit_thenBlocks() {
        String ip = "10.0.0.1";
        long now = 1_000_000L;

        // As primeiras MAX_REQUESTS_PER_WINDOW requisicoes sao permitidas.
        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            assertThat(filter.isLimitExceeded(ip, now))
                    .as("requisicao %d deveria ser permitida", i + 1)
                    .isFalse();
        }

        // A requisicao seguinte ultrapassa o limite.
        assertThat(filter.isLimitExceeded(ip, now)).isTrue();
    }

    @Test
    void resetsCountAfterWindowExpires() {
        String ip = "10.0.0.2";
        long start = 2_000_000L;

        // Esgota o limite na janela atual.
        for (int i = 0; i < MAX_REQUESTS_PER_WINDOW; i++) {
            filter.isLimitExceeded(ip, start);
        }
        assertThat(filter.isLimitExceeded(ip, start)).isTrue();

        // Apos a janela expirar, a contagem reinicia e o cliente e liberado.
        long afterWindow = start + WINDOW_SECONDS;
        assertThat(filter.isLimitExceeded(ip, afterWindow)).isFalse();
    }

    @Test
    void evictsExpiredEntries_preventingMemoryLeak() {
        long start = 3_000_000L;

        // Cria janelas para 100 IPs distintos que nunca mais retornam.
        for (int i = 0; i < 100; i++) {
            filter.isLimitExceeded("192.168.0." + i, start);
        }
        assertThat(filter.trackedClients()).isEqualTo(100);

        // Passado o intervalo de limpeza (e a janela), a varredura remove as entradas antigas.
        long afterCleanup = start + CLEANUP_INTERVAL_SECONDS + WINDOW_SECONDS;
        filter.evictExpiredIfDue(afterCleanup);

        assertThat(filter.trackedClients())
                .as("entradas expiradas devem ser removidas para evitar memory leak")
                .isZero();
    }

    @Test
    void keepsActiveEntriesDuringCleanup() {
        long start = 4_000_000L;
        filter.isLimitExceeded("172.16.0.1", start); // entrada antiga (sera removida)

        // Avanca ate disparar a limpeza, mas registra um IP ativo nesse mesmo instante.
        long cleanupTime = start + CLEANUP_INTERVAL_SECONDS + WINDOW_SECONDS;
        filter.isLimitExceeded("172.16.0.2", cleanupTime); // entrada nova (deve permanecer)

        assertThat(filter.trackedClients()).isEqualTo(1);
    }
}
