package io.github.lucasfcz.olympusprotocol.configs;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        RedisCacheConfiguration defaultConfig =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(30))
                        .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)

                // Workout Plans
                .withCacheConfiguration(
                        "active-workout-plan",
                        defaultConfig.entryTtl(Duration.ofMinutes(10))
                )
                .withCacheConfiguration(
                        "user-workout-plans",
                        defaultConfig.entryTtl(Duration.ofMinutes(15))
                )
                .withCacheConfiguration(
                        "workout-plan",
                        defaultConfig.entryTtl(Duration.ofMinutes(20))
                )

                // Workout Sessions
                .withCacheConfiguration(
                        "workout-session",
                        defaultConfig.entryTtl(Duration.ofMinutes(5))
                )
                .withCacheConfiguration(
                        "user-workout-sessions",
                        defaultConfig.entryTtl(Duration.ofMinutes(5))
                )
                .withCacheConfiguration(
                        "session-summary",
                        defaultConfig.entryTtl(Duration.ofHours(1))
                )

                // Stats
                .withCacheConfiguration(
                        "user-stats",
                        defaultConfig.entryTtl(Duration.ofMinutes(5))
                )
                .withCacheConfiguration(
                        "muscle-volume",
                        defaultConfig.entryTtl(Duration.ofMinutes(5))
                )
                .withCacheConfiguration(
                        "exercise-stats",
                        defaultConfig.entryTtl(Duration.ofMinutes(5))
                )
                .withCacheConfiguration(
                        "weekly-volume",
                        defaultConfig.entryTtl(Duration.ofMinutes(5))
                )
                .withCacheConfiguration(
                        "monthly-frequency",
                        defaultConfig.entryTtl(Duration.ofMinutes(10))
                )

                // Users
                .withCacheConfiguration(
                        "user-profile",
                        defaultConfig.entryTtl(Duration.ofMinutes(30))
                )
                .withCacheConfiguration(
                        "public-user-profile",
                        defaultConfig.entryTtl(Duration.ofHours(1))
                )
                .withCacheConfiguration(
                        "users-by-name",
                        defaultConfig.entryTtl(Duration.ofMinutes(10))
                )

                // Exercises
                .withCacheConfiguration(
                        "exercise",
                        defaultConfig.entryTtl(Duration.ofHours(6))
                )
                .withCacheConfiguration(
                        "exercises",
                        defaultConfig.entryTtl(Duration.ofHours(2))
                )

                .build();
    }
}
