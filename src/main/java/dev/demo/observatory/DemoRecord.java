package dev.demo.observatory;

import java.time.OffsetDateTime;

public record DemoRecord(int id, String title, String sourceReference, OffsetDateTime observedAt, String quality) {
}
