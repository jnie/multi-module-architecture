package dk.jnie.example.repository;

import dk.jnie.example.domain.repository.CacheRepository;
import io.r2dbc.h2.H2ConnectionFactory;
import io.r2dbc.h2.H2ConnectionConfiguration;
import io.r2dbc.spi.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.core.DefaultReactiveDataAccessStrategy;
import org.springframework.data.r2dbc.dialect.H2Dialect;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Slf4j
public class CacheRepositoryImpl implements CacheRepository {

    private final DatabaseClient databaseClient;

    public CacheRepositoryImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
        initializeSchema();
    }

    private void initializeSchema() {
        databaseClient.sql("""
            CREATE TABLE IF NOT EXISTS cache_entries (
                cache_key VARCHAR(512) PRIMARY KEY,
                cache_value TEXT NOT NULL,
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """)
            .fetch()
            .rowsUpdated()
            .block();
    }

    @Override
    public Mono<String> get(String key) {
        return databaseClient.sql("SELECT cache_value FROM cache_entries WHERE cache_key = :key")
                .bind("key", key)
                .map((row, metadata) -> row.get("cache_value", String.class))
                .first()
                .doOnNext(value -> log.debug("Cache hit for key: {}", key))
                .switchIfEmpty(Mono.defer(() -> {
                    log.debug("Cache miss for key: {}", key);
                    return Mono.empty();
                }));
    }

    @Override
    public Mono<Boolean> put(String key, String value) {
        return databaseClient.sql("""
                MERGE INTO cache_entries (cache_key, cache_value, created_at)
                VALUES (:key, :value, :createdAt)
                """)
                .bind("key", key)
                .bind("value", value)
                .bind("createdAt", Instant.now())
                .fetch()
                .rowsUpdated()
                .map(rowsUpdated -> rowsUpdated > 0)
                .doOnNext(success -> {
                    if (success) {
                        log.debug("Cached value for key: {}", key);
                    }
                });
    }

    @Override
    public Mono<Boolean> evict(String key) {
        return databaseClient.sql("DELETE FROM cache_entries WHERE cache_key = :key")
                .bind("key", key)
                .fetch()
                .rowsUpdated()
                .map(rowsUpdated -> rowsUpdated > 0)
                .doOnNext(success -> {
                    if (success) {
                        log.debug("Evicted cache for key: {}", key);
                    }
                });
    }

    @Override
    public Mono<Boolean> exists(String key) {
        return databaseClient.sql("SELECT COUNT(*) as cnt FROM cache_entries WHERE cache_key = :key")
                .bind("key", key)
                .map((row, metadata) -> row.get("cnt", Long.class) > 0)
                .first()
                .defaultIfEmpty(false);
    }

    @Configuration
    public static class CacheRepositoryConfig {

        @Bean
        public ConnectionFactory connectionFactory() {
            H2ConnectionConfiguration config = H2ConnectionConfiguration.builder()
                    .url("mem:mma-cache;DB_CLOSE_DELAY=-1")
                    .username("sa")
                    .password("")
                    .build();
            return new H2ConnectionFactory(config);
        }

        @Bean
        public DatabaseClient databaseClient(ConnectionFactory connectionFactory) {
            DefaultReactiveDataAccessStrategy strategy = new DefaultReactiveDataAccessStrategy(H2Dialect.INSTANCE);
            return DatabaseClient.create(connectionFactory);
        }

        @Bean
        public CacheRepository cacheRepository(DatabaseClient databaseClient) {
            return new CacheRepositoryImpl(databaseClient);
        }
    }
}