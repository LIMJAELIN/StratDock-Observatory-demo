package dev.demo.observatory;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DemoRecordService {
    private final JdbcClient jdbc;

    public DemoRecordService(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<DemoRecord> listRecords() {
        try {
            List<DemoRecord> records = jdbc.sql("""
                            SELECT record_id, title, source_reference, observed_at
                            FROM demo_record
                            ORDER BY record_id
                            LIMIT 100
                            """)
                    .query((row, index) -> new DemoRecord(
                            row.getInt("record_id"),
                            row.getString("title"),
                            row.getString("source_reference"),
                            row.getObject("observed_at", java.time.OffsetDateTime.class),
                            ""
                    )).list();

            Map<String, Integer> sourceCounts = new HashMap<>();
            records.stream().map(DemoRecord::sourceReference).filter(ref -> ref != null)
                    .forEach(ref -> sourceCounts.merge(ref, 1, Integer::sum));

            return records.stream().map(record -> {
                String quality = record.sourceReference() == null
                        ? "MISSING_SOURCE"
                        : sourceCounts.get(record.sourceReference()) > 1 ? "DUPLICATE_SOURCE" : "LINKED";
                return new DemoRecord(record.id(), record.title(), record.sourceReference(), record.observedAt(), quality);
            }).toList();
        } catch (DataAccessException exception) {
            throw new DemoSourceUnavailableException();
        }
    }
}
