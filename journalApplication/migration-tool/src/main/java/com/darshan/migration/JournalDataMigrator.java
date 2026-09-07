package com.darshan.migration;

import java.sql.*;

public final class JournalDataMigrator {
    public MigrationReport migrate(Connection source, Connection target, boolean execute) throws SQLException {
        Counts sourceCounts = counts(source);
        long journalOrphans = count(source, "SELECT COUNT(*) FROM journal_entries j LEFT JOIN users u ON u.id=j.user_id WHERE j.user_id IS NULL OR u.id IS NULL");
        long tagOrphans = count(source, "SELECT COUNT(*) FROM tags t LEFT JOIN users u ON u.id=t.user_id WHERE u.id IS NULL");
        if (journalOrphans > 0 || tagOrphans > 0) throw new IllegalStateException("Source contains orphaned owners");
        if (!counts(target).equals(new Counts(0, 0, 0))) throw new IllegalStateException("Target journal tables must be empty");
        if (!execute) return report("DRY_RUN", sourceCounts, journalOrphans, tagOrphans);

        boolean autoCommit = target.getAutoCommit();
        target.setAutoCommit(false);
        try {
            copy(source, target, "SELECT id,user_id,title,content,date,created_at,updated_at,favorite FROM journal_entries ORDER BY id",
                    "INSERT INTO journal_entries(id,owner_id,title,content,date,created_at,updated_at,favorite) VALUES(?,?,?,?,?,?,?,?)", 8);
            copy(source, target, "SELECT id,user_id,name,normalized_name,created_at FROM tags ORDER BY id",
                    "INSERT INTO tags(id,owner_id,name,normalized_name,created_at) VALUES(?,?,?,?,?)", 5);
            copy(source, target, "SELECT journal_entry_id,tag_id FROM journal_entry_tags ORDER BY journal_entry_id,tag_id",
                    "INSERT INTO journal_entry_tags(journal_entry_id,tag_id) VALUES(?,?)", 2);
            Counts targetCounts = counts(target);
            if (!sourceCounts.equals(targetCounts)) throw new IllegalStateException("Source and target counts differ");
            target.commit();
            return report("EXECUTE", targetCounts, 0, 0);
        } catch (SQLException | RuntimeException exception) {
            target.rollback();
            throw exception;
        } finally {
            target.setAutoCommit(autoCommit);
        }
    }

    private MigrationReport report(String mode, Counts c, long jo, long to) {
        return new MigrationReport(mode, c.journals, c.tags, c.links, jo, to);
    }
    private Counts counts(Connection c) throws SQLException {
        return new Counts(count(c,"SELECT COUNT(*) FROM journal_entries"), count(c,"SELECT COUNT(*) FROM tags"), count(c,"SELECT COUNT(*) FROM journal_entry_tags"));
    }
    private long count(Connection c, String sql) throws SQLException {
        try (Statement s=c.createStatement(); ResultSet r=s.executeQuery(sql)) { r.next(); return r.getLong(1); }
    }
    private void copy(Connection source, Connection target, String query, String insertSql, int columns) throws SQLException {
        try (Statement select=source.createStatement(); ResultSet rows=select.executeQuery(query); PreparedStatement insert=target.prepareStatement(insertSql)) {
            while(rows.next()) { for(int i=1;i<=columns;i++) insert.setObject(i,rows.getObject(i)); insert.addBatch(); }
            insert.executeBatch();
        }
    }
    private record Counts(long journals, long tags, long links) {}
}
