package backend.academy.scrapper;

import backend.academy.scrapper.config.kafka.KafkaConfigProperties;
import backend.academy.scrapper.config.resilience.ResilienceConfigProperties;
import backend.academy.scrapper.config.scrapper.ScrapperConfigProperties;
import java.sql.SQLException;
import javax.sql.DataSource;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
    ScrapperConfigProperties.class,
    KafkaConfigProperties.class,
    ResilienceConfigProperties.class
})
@Slf4j
public class ScrapperApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScrapperApplication.class, args);
    }
}
