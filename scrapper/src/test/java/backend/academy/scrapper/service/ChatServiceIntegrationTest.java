package backend.academy.scrapper.service;

import backend.academy.scrapper.TestcontainersConfiguration;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@Import(TestcontainersConfiguration.class)
public class ChatServiceIntegrationTest {}
