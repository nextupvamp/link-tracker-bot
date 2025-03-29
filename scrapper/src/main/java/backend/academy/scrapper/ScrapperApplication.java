package backend.academy.scrapper;

import javax.sql.DataSource;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({ScrapperConfigProperties.class})
@EnableScheduling
@Slf4j
public class ScrapperApplication {
    public static void main(String[] args) {
        var context = SpringApplication.run(ScrapperApplication.class, args);
        var dataSource = context.getBean(DataSource.class);
        try (var connection = dataSource.getConnection()) {
            Database database =
                    DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

            DatabaseChangeLog changelog = new DatabaseChangeLog("migrations/master.xml");

            Liquibase liquibase = new Liquibase(changelog, new ClassLoaderResourceAccessor(), database);

            liquibase.update(new Contexts(), new LabelExpression());
        } catch (Exception e) {
            log.info("Liquibase migration has been failed");
        }
        log.info("Liquibase migration has been executed successfully");
    }
}
