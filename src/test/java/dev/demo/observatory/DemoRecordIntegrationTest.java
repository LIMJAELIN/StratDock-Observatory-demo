package dev.demo.observatory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.sql.DriverManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class DemoRecordIntegrationTest {
    @Container
    static final PostgreSQLContainer DATABASE = new PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("demo_catalog")
            .withUsername("demo_admin")
            .withPassword("test-admin-only")
            .withInitScript("demo-test-init.sql");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", DATABASE::getJdbcUrl);
        registry.add("spring.datasource.username", () -> "demo_reader");
        registry.add("spring.datasource.password", () -> "test-reader-only");
    }

    @Autowired
    DemoRecordService service;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void resetFixture() throws Exception {
        try (var connection = DriverManager.getConnection(
                DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword());
             var statement = connection.createStatement()) {
            statement.executeUpdate("TRUNCATE demo_record");
            statement.executeUpdate("""
                    INSERT INTO demo_record (record_id, title, source_reference, observed_at) VALUES
                    (1, 'normal sample', 'ref-1', '2026-01-12T09:30:00Z'),
                    (2, 'duplicate sample one', 'ref-2', '2026-01-12T09:35:00Z'),
                    (3, 'duplicate sample two', 'ref-2', '2026-01-12T09:36:00Z'),
                    (4, 'missing source sample', NULL, '2026-01-12T09:40:00Z')
                    """);
        }
    }

    @Test
    void readsSyntheticRecordsAndKeepsReaderRoleSelectOnly() {
        var records = service.listRecords();

        assertThat(records).hasSize(4);
        assertThat(records).extracting(DemoRecord::quality)
                .containsExactly("LINKED", "DUPLICATE_SOURCE", "DUPLICATE_SOURCE", "MISSING_SOURCE");
        assertThat(jdbc.queryForObject(
                "SELECT has_table_privilege(current_user, 'demo_record', 'INSERT')", Boolean.class)).isFalse();
        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO demo_record (record_id, title, source_reference, observed_at) VALUES (5, 'write', 'ref', now())"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void returnsAnEmptyListWhenTheSourceHasNoRows() throws Exception {
        try (var connection = DriverManager.getConnection(
                DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword());
             var statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM demo_record");
        }

        assertThat(service.listRecords()).isEmpty();
    }
}
