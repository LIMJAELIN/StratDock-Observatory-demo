CREATE ROLE demo_reader LOGIN PASSWORD 'test-reader-only';
CREATE TABLE demo_record (
    record_id integer PRIMARY KEY,
    title text NOT NULL,
    source_reference text,
    observed_at timestamptz NOT NULL
);
INSERT INTO demo_record (record_id, title, source_reference, observed_at) VALUES
    (1, 'normal sample', 'ref-1', '2026-01-12T09:30:00Z'),
    (2, 'duplicate sample one', 'ref-2', '2026-01-12T09:35:00Z'),
    (3, 'duplicate sample two', 'ref-2', '2026-01-12T09:36:00Z'),
    (4, 'missing source sample', NULL, '2026-01-12T09:40:00Z');
GRANT USAGE ON SCHEMA public TO demo_reader;
GRANT SELECT ON TABLE demo_record TO demo_reader;
ALTER ROLE demo_reader SET default_transaction_read_only = on;
