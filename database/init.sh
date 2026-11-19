#!/bin/sh
set -eu

psql --set=ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" \
  --set=reader_password="$DEMO_SOURCE_PASSWORD" <<'SQL'
CREATE ROLE demo_reader LOGIN PASSWORD :'reader_password';

CREATE TABLE demo_record (
    record_id integer PRIMARY KEY,
    title text NOT NULL,
    source_reference text,
    observed_at timestamptz NOT NULL
);

INSERT INTO demo_record (record_id, title, source_reference, observed_at) VALUES
    (1, '정상 출처가 연결된 예시', 'sample-source-01', '2026-01-12T09:30:00Z'),
    (2, '중복 출처 예시 A', 'sample-source-02', '2026-01-12T09:35:00Z'),
    (3, '중복 출처 예시 B', 'sample-source-02', '2026-01-12T09:36:00Z'),
    (4, '출처 정보가 비어 있는 예시', NULL, '2026-01-12T09:40:00Z');

GRANT USAGE ON SCHEMA public TO demo_reader;
GRANT SELECT ON TABLE demo_record TO demo_reader;
ALTER ROLE demo_reader SET default_transaction_read_only = on;
SQL
