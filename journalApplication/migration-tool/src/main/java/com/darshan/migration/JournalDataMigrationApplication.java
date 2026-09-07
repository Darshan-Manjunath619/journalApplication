package com.darshan.migration;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.Properties;

public final class JournalDataMigrationApplication {
    private JournalDataMigrationApplication() {}

    public static void main(String[] args) throws Exception {
        Properties values = new Properties();
        Path file = Path.of(System.getenv().getOrDefault("MIGRATION_ENV_FILE", "../.env"));
        if (Files.isRegularFile(file)) {
            try (InputStream input = Files.newInputStream(file)) { values.load(input); }
        }
        String sourceUrl = required("DB_URL", values);
        String targetUrl = required("JOURNAL_DB_URL", values);
        try (var source = DriverManager.getConnection(sourceUrl, required("DB_USERNAME", values), required("DB_PASSWORD", values));
             var target = DriverManager.getConnection(targetUrl, required("JOURNAL_DB_USERNAME", values), required("JOURNAL_DB_PASSWORD", values))) {
            System.out.print(new JournalDataMigrator().migrate(
                    source, target, Arrays.asList(args).contains("--execute")).format());
        }
    }

    private static String required(String name, Properties values) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) value = values.getProperty(name);
        if (value == null || value.isBlank()) throw new IllegalStateException("Missing configuration: " + name);
        return value;
    }
}
