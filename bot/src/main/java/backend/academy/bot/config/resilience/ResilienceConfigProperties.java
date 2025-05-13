package backend.academy.bot.config.resilience;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.resilience")
public record ResilienceConfigProperties(
        int retryMaxAttempts,
        int retryWaitDuration,
        int timeLimiterTimeoutDuration,
        int circuitBreakerSlidingWindowSize,
        int circuitBreakerMinimumNumberOfCalls,
        int circuitBreakerFailureRateThreshold,
        int circuitBreakerPermittedCallsInHalfOpenState,
        int circuitBreakerSlowCallDurationThreshold,
        int circuitBreakerWaitDurationInOpenState) {}
