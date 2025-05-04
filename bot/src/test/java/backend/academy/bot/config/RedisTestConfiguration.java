package backend.academy.bot.config;

import java.util.List;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class RedisTestConfiguration {
    @Bean
    @RestartScope
    public GenericContainer<?> redisContainer() {
        var container = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"));
        container.setPortBindings(List.of("6379:6379"));
        return container;
    }
}
