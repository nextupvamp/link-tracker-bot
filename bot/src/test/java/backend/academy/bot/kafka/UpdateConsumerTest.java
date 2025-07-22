package backend.academy.bot.kafka;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import backend.academy.bot.config.KafkaTestConfiguration;
import backend.academy.bot.dto.LinkUpdate;
import backend.academy.bot.service.UpdateSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = {UpdateConsumer.class})
@Import(KafkaTestConfiguration.class)
@TestPropertySource(properties = {"app.kafka.topic=updates-test", "app.enable-kafka=true"})
public class UpdateConsumerTest {

    private static final String TOPIC = "updates-test";

    @MockitoBean
    private UpdateSender updateSender;

    @Autowired
    private KafkaTemplate<String, LinkUpdate> defaultKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @SneakyThrows
    @BeforeEach
    public void warmup() {
        /*
           For some reason one of the tests will randomly fall if there's no such
           warmup
        */
        defaultKafkaTemplate.send(TOPIC, 0, null, null);
        Thread.sleep(1_000);
    }

    @Test
    @SneakyThrows
    public void testConsumeUpdate() {
        var update = new LinkUpdate("url", "topic", "username", 1, "preview", null);

        defaultKafkaTemplate.send(TOPIC, 0, null, update).get();
        defaultKafkaTemplate.flush();

        Thread.sleep(1_000);

        verify(updateSender).sendUpdates(eq(update));
    }

    @Test
    @SneakyThrows
    public void testConsumeJson() {
        var update = new LinkUpdate("url", "topic", "username", 1, "preview", null);
        String json = new ObjectMapper().writeValueAsString(update);

        kafkaTemplate.send("updates-test", 0, null, json);
        kafkaTemplate.flush();

        Thread.sleep(1_000);

        verify(updateSender).sendUpdates(eq(update));
    }

    @Test
    @SneakyThrows
    public void testConsumeInvalid() {
        var data = "abcd";

        kafkaTemplate.send("updates-test", 0, null, data);
        kafkaTemplate.flush();

        Thread.sleep(1_000);

        verify(updateSender, never()).sendUpdates(any(LinkUpdate.class));
    }
}
