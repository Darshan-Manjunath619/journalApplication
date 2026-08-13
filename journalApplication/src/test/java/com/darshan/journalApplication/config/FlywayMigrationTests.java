package com.darshan.journalApplication.config;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class FlywayMigrationTests {

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void appliesVersionOneAndCreatesLegacyTables() {
        assertEquals("1", flyway.info().current().getVersion().getVersion());
        assertEquals(1, tableCount("USERS"));
        assertEquals(1, tableCount("JOURNAL_ENTRIES"));
        assertEquals(1, tableCount("USER_ROLES"));
    }

    private int tableCount(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = SCHEMA() AND TABLE_NAME = ?",
                Integer.class, tableName);
        return count == null ? 0 : count;
    }
}
