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
    void appliesAllMigrationsAndCreatesExpectedTables() {
        assertEquals("6", flyway.info().current().getVersion().getVersion());
        assertEquals(1, tableCount("USERS"));
        assertEquals(1, tableCount("JOURNAL_ENTRIES"));
        assertEquals(1, tableCount("USER_ROLES"));
        assertEquals(1, tableCount("REFRESH_TOKENS"));
        assertEquals(1, tableCount("TAGS"));
        assertEquals(1, tableCount("JOURNAL_ENTRY_TAGS"));
        assertEquals(1, columnCount("JOURNAL_ENTRIES", "CREATED_AT"));
        assertEquals(1, columnCount("JOURNAL_ENTRIES", "UPDATED_AT"));
        assertEquals(1, columnCount("JOURNAL_ENTRIES", "FAVORITE"));
    }

    private int tableCount(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = SCHEMA() AND TABLE_NAME = ?",
                Integer.class, tableName);
        return count == null ? 0 : count;
    }

    private int columnCount(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                        "WHERE TABLE_SCHEMA = SCHEMA() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                Integer.class, tableName, columnName);
        return count == null ? 0 : count;
    }
}
