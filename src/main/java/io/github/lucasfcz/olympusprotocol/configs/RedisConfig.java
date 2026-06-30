package io.github.lucasfcz.olympusprotocol.configs;

import io.github.lucasfcz.olympusprotocol.cache.CachesNames;
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
                        CachesNames.ACTIVE_WORKOUT_PLAN,
                        defaultConfig.entryTtl(Duration.ofMinutes(10))
                )
                .withCacheConfiguration(
                        CachesNames.USER_WORKOUT_PLANS,
                        defaultConfig.entryTtl(Duration.ofMinutes(15))
                )
                .withCacheConfiguration(
                        CachesNames.WORKOUT_PLAN,
                        defaultConfig.entryTtl(Duration.ofMinutes(20))
                )

                // Workout Sessions
                .withCacheConfiguration(
                        CachesNames.WORKOUT_SESSION,
                        defaultConfig.entryTtl(Duration.ofMinutes(5))
                )
                .withCacheConfiguration(
                        CachesNames.USER_WORKOUT_SESSIONS,
                        defaultConfig.entryTtl(Duration.ofMinutes(5))
                )
                .withCacheConfiguration(
                        CachesNames.SESSION_SUMMARY,
                        defaultConfig.entryTtl(Duration.ofHours(1))
                )

                // Stats
                .withCacheConfiguration(
                        CachesNames.USER_STATS,
                        defaultConfig.entryTtl(Duration.ofMinutes(5))
                )
                .withCacheConfiguration(
                        CachesNames.MUSCLE_VOLUME,
                        defaultConfig.entryTtl(Duration.ofMinutes(5))
                )
                .withCacheConfiguration(
                        CachesNames.EXERCISE_STATS,
                        defaultConfig.entryTtl(Duration.ofMinutes(5))
                )
                .withCacheConfiguration(
                        CachesNames.WEEKLY_VOLUME,
                        defaultConfig.entryTtl(Duration.ofMinutes(5))
                )
                .withCacheConfiguration(
                        CachesNames.MONTHLY_FREQUENCY,
                        defaultConfig.entryTtl(Duration.ofMinutes(10))
                )

                // Users
                .withCacheConfiguration(
                        CachesNames.USER_PROFILE,
                        defaultConfig.entryTtl(Duration.ofMinutes(30))
                )
                .withCacheConfiguration(
                        CachesNames.PUBLIC_USER_PROFILE,
                        defaultConfig.entryTtl(Duration.ofHours(1))
                )
                .withCacheConfiguration(
                        CachesNames.USERS_BY_NAME,
                        defaultConfig.entryTtl(Duration.ofMinutes(10))
                )

                // Exercises
                .withCacheConfiguration(
                        CachesNames.EXERCISE,
                        defaultConfig.entryTtl(Duration.ofHours(6))
                )
                .withCacheConfiguration(
                        CachesNames.EXERCISES,
                        defaultConfig.entryTtl(Duration.ofHours(2))
                )

                .build();
    }
}