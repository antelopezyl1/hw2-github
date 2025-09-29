package org.example.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.sqlite.SQLiteDataSource;

import jakarta.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Types;
import java.time.Instant;

@Component
@Primary
public class MemoryWebhookStore implements WebhookStore {

    private static final Logger log = LoggerFactory.getLogger(MemoryWebhookStore.class);
    
    // Spring's convenience wrapper for JDBC operations. 
    private final JdbcTemplate jdbc;
    
    // Absolute path to the SQLite DB file (useful for logs/diagnostics)
    private final Path dbFile;

    /**
     * Construct the store and initialize a DataSource pointing at a file-based SQLite DB.
     */
    public MemoryWebhookStore(
            @Value("${webhook.db.path:${WEBHOOK_DB_PATH:./data/webhooks.db}}") String dbPath
    ) throws Exception {
        this.dbFile = Path.of(dbPath).toAbsolutePath();
        Files.createDirectories(dbFile.getParent()); // ensure folder exists
        SQLiteDataSource ds = new SQLiteDataSource();
        ds.setUrl("jdbc:sqlite:" + dbFile);
        this.jdbc = new JdbcTemplate(ds);
        log.info("SQLite webhook store using {}", dbFile);
    }
    
    /**
     * Initialize the schema at application startup.
     *
     * Creates the {@code webhook_events} table if it doesn't exist, and a UNIQUE index on
     * {@code (delivery_id, action)} to guarantee idempotency across retries.
     */
    @PostConstruct
    void initSchema() {
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS webhook_events (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              delivery_id  TEXT NOT NULL,
              event        TEXT NOT NULL,
              action       TEXT NOT NULL,
              issue_number TEXT,
              received_at  TEXT NOT NULL,
              payload      BLOB NOT NULL
            );
        """);
        jdbc.execute("""
            CREATE UNIQUE INDEX IF NOT EXISTS ux_webhook_events_delivery_action
            ON webhook_events(delivery_id, action);
        """);
        log.info("SQLite schema ready (webhook_events) at {}", dbFile);
    }
    
    /**
     * Persist a webhook event if it's not already present.
     *
     * Uses {@code INSERT OR IGNORE} so duplicates (by UNIQUE key) do not throw and simply return 0 rows affected.
     *
     * @param deliveryId  X-GitHub-Delivery header value
     * @param event       X-GitHub-Event header value
     * @param action      payload action (or "ping" for ping events)
     * @param issueNumber optional issue number for convenience
     * @param payload     raw JSON payload bytes
     * @return true if a new row was inserted; false if it was a duplicate
     */
    @Override
    public boolean saveIfAbsent(String deliveryId, String event, String action, String issueNumber, byte[] payload) {
        String sql = """
            INSERT OR IGNORE INTO webhook_events
              (delivery_id, event, action, issue_number, received_at, payload)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        Object[] args = { deliveryId, event, action, issueNumber, Instant.now().toString(), payload };
        int[] types   = { Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.BLOB };
        boolean created = jdbc.update(sql, args, types) == 1;
        log.debug("sqlite.store put delivery={} action={} created={}", deliveryId, action, created);
        return created;
    }
}

