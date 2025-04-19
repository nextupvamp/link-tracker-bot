package backend.academy.bot.kafka;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import backend.academy.bot.config.KafkaTestConfiguration;
import backend.academy.bot.dto.LinkUpdate;
import backend.academy.bot.service.UpdateSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = {UpdateConsumer.class})
@Import(KafkaTestConfiguration.class)
@TestPropertySource(properties = {"app.kafka.topic=updates-test", "app.kafka.partitions=0"})
public class UpdateConsumerTest {
    @MockitoBean
    private UpdateSender updateSender;

    @Autowired
    private UpdateConsumer updateConsumer;

    @Autowired
    private KafkaTemplate<String, LinkUpdate> defaultKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    @Test
    @SneakyThrows
    public void testConsumeUpdate() {
        var update = new LinkUpdate("url", "topic", "username", 1, "preview", null);

        defaultKafkaTemplate.send("updates-test", 0, null, update).get();

        Thread.sleep(1_000);

        verify(updateSender).sendUpdates(eq(update));
    }

    @Test
    @SneakyThrows
    public void testConsumeJson() {
        var update = new LinkUpdate("url", "topic", "username", 1, "preview", null);
        String json = new ObjectMapper().writeValueAsString(update);

        kafkaTemplate.send("updates-test", 0, null, json.getBytes(StandardCharsets.UTF_8));

        Thread.sleep(1_000);

        verify(updateSender).sendUpdates(eq(update));
    }

    @Test
    @SneakyThrows
    public void testConsumeInvalid() {
        var data = new byte[] {};

        kafkaTemplate.send("updates-test", 0, null, data);

        Thread.sleep(1_000);

        verify(updateSender, never()).sendUpdates(any());
    }
}
