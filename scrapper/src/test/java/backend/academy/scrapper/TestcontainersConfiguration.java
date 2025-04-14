package backend.academy.scrapper;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("scrapper")
            .withUsername("postgres")
            .withPassword("postgres");

    @BeforeAll
    static void setUp() {
        postgreSQLContainer.start();
        cleanDatabase();
        runLiquibaseMigrations();
    }

    private static void runLiquibaseMigrations() {
        try (Connection connection = postgreSQLContainer.createConnection("")) {
            Database database = new PostgresDatabase();
            database.setConnection(new liquibase.database.jvm.JdbcConnection(connection));

            Liquibase liquibase = new Liquibase(
                    "classpath:db/changelog/db.changelog-master.xml", new ClassLoaderResourceAccessor(), database);
            liquibase.update("");
        } catch (LiquibaseException | SQLException e) {
            throw new RuntimeException("Ошибка при запуске Liquibase", e);
        }
    }
//
//    @Bean
//    @RestartScope
//    @ServiceConnection(name = "backend/academy/scrapper/redis")
//    GenericContainer<?> redisContainer() {
//        return new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);
//    }

    @Bean
    @RestartScope
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return postgreSQLContainer;
    }

//    @Bean
//    @RestartScope
//    @ServiceConnection
//    KafkaContainer kafkaContainer() {
//        return new KafkaContainer("apache/kafka-native:3.8.1").withExposedPorts(9092);
//    }

    private static void cleanDatabase() {
        try (Connection connection = postgreSQLContainer.createConnection("")) {
            Statement statement = connection.createStatement();
            statement.executeUpdate("TRUNCATE TABLE link_tag, link, chat, tag RESTART IDENTITY CASCADE");
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при очистке базы данных", e);
        }
    }

    //    @Bean
    //    SpringLiquibase liquibase(DataSource dataSource) {
    //        SpringLiquibase liquibase = new SpringLiquibase();
    //        liquibase.setDataSource(dataSource);
    //        liquibase.setChangeLog("classpath:db/changelog/db.changelog-master.xml");
    //        return liquibase;
    //    }
}
