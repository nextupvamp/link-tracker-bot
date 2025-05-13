package backend.academy.scrapper.config.resilience;

import backend.academy.scrapper.exception.RetryableException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import java.time.Duration;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@AllArgsConstructor
public class ResilienceConfig {
    public static final String RETRY_NAME = "default";
    public static final String CIRCUIT_BREAKER_NAME = "default";
    public static final String RATE_LIMITER_NAME = "default";

    private ResilienceConfigProperties config;

    @Bean
    public Retry retry() {
        RetryConfig retryConfig = RetryConfig.custom()
                .retryOnException(e -> e.getClass() == RetryableException.class)
                .maxAttempts(config.retryMaxAttempts())
                .waitDuration(Duration.ofMillis(config.retryWaitDuration()))
                .build();

        return Retry.of(RETRY_NAME, retryConfig);
    }

    @Bean
    public CircuitBreaker circuitBreaker() {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .slidingWindowSize(config.circuitBreakerSlidingWindowSize())
                .minimumNumberOfCalls(config.circuitBreakerMinimumNumberOfCalls())
                .failureRateThreshold(config.circuitBreakerFailureRateThreshold())
                .permittedNumberOfCallsInHalfOpenState(config.circuitBreakerPermittedCallsInHalfOpenState())
                .waitDurationInOpenState(Duration.ofSeconds(config.circuitBreakerWaitDurationInOpenState()))
                .slowCallDurationThreshold(Duration.ofSeconds(config.circuitBreakerSlowCallDurationThreshold()))
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                .build();

        return CircuitBreaker.of(CIRCUIT_BREAKER_NAME, circuitBreakerConfig);
    }

    @Bean
    public TimeLimiter timeLimiter() {
        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(config.timeLimiterTimeoutDuration()))
                .build();

        return TimeLimiter.of(timeLimiterConfig);
    }

    @Component
    public record ResilienceFeatures(TimeLimiter timeLimiter, CircuitBreaker circuitBreaker, Retry retry) {}
}
