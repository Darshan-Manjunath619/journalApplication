package com.darshan.migration;

import org.junit.jupiter.api.Test;
import java.sql.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class JournalDataMigratorTests {
    @Test void dryRunAndExecutionPreserveIdsAndOwners() throws Exception {
        try (Connection source=db(); Connection target=db()) {
            schemas(source,target); seed(source);
            MigrationReport preview=new JournalDataMigrator().migrate(source,target,false);
            assertEquals("DRY_RUN",preview.mode());
            assertEquals(0,count(target,"journal_entries"));
            MigrationReport result=new JournalDataMigrator().migrate(source,target,true);
            assertEquals(1,result.journals());
            try (ResultSet row=target.createStatement().executeQuery("SELECT id,owner_id FROM journal_entries")) {
                assertTrue(row.next()); assertEquals(100,row.getLong(1)); assertEquals(42,row.getLong(2));
            }
        }
    }

    @Test void refusesNonEmptyTarget() throws Exception {
        try (Connection source=db(); Connection target=db()) {
            schemas(source,target); seed(source);
            target.createStatement().execute("INSERT INTO journal_entries(id,owner_id,title,created_at,updated_at,favorite) VALUES(1,1,'existing',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,FALSE)");
            assertThrows(IllegalStateException.class,()->new JournalDataMigrator().migrate(source,target,true));
        }
    }

    private Connection db() throws SQLException {
        return DriverManager.getConnection("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;DATABASE_TO_LOWER=TRUE");
    }
    private void schemas(Connection s,Connection t) throws SQLException {
        execute(s,"CREATE TABLE users(id BIGINT PRIMARY KEY)",
          "CREATE TABLE journal_entries(id BIGINT PRIMARY KEY,user_id BIGINT,title VARCHAR(255),content TEXT,date DATETIME,created_at TIMESTAMP,updated_at TIMESTAMP,favorite BOOLEAN)",
          "CREATE TABLE tags(id BIGINT PRIMARY KEY,user_id BIGINT,name VARCHAR(80),normalized_name VARCHAR(80),created_at TIMESTAMP)",
          "CREATE TABLE journal_entry_tags(journal_entry_id BIGINT,tag_id BIGINT)");
        execute(t,
          "CREATE TABLE journal_entries(id BIGINT PRIMARY KEY,owner_id BIGINT,title VARCHAR(255),content TEXT,date DATETIME,created_at TIMESTAMP,updated_at TIMESTAMP,favorite BOOLEAN)",
          "CREATE TABLE tags(id BIGINT PRIMARY KEY,owner_id BIGINT,name VARCHAR(80),normalized_name VARCHAR(80),created_at TIMESTAMP)",
          "CREATE TABLE journal_entry_tags(journal_entry_id BIGINT,tag_id BIGINT)");
    }
    private void seed(Connection c) throws SQLException {
        execute(c,"INSERT INTO users VALUES(42)",
          "INSERT INTO journal_entries VALUES(100,42,'Learning','Flyway',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,TRUE)",
          "INSERT INTO tags VALUES(200,42,'Java','java',CURRENT_TIMESTAMP)",
          "INSERT INTO journal_entry_tags VALUES(100,200)");
    }
    private void execute(Connection c,String... sql) throws SQLException { try(Statement s=c.createStatement()){for(String q:sql)s.execute(q);} }
    private long count(Connection c,String table) throws SQLException {try(ResultSet r=c.createStatement().executeQuery("SELECT COUNT(*) FROM "+table)){r.next();return r.getLong(1);}}
}
