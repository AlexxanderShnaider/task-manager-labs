package com.example.service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TaskAuditTrail {

    private static final Logger log =
            LoggerFactory.getLogger(TaskAuditTrail.class);

    private final List<Entry> entries = new CopyOnWriteArrayList<>();

    public void add(String action, Long taskId) {
        Entry entry = new Entry(action, taskId, Instant.now());
        entries.add(entry);

        log.info("Task audit: action={}, taskId={}", action, taskId);
    }

    public List<Entry> getEntries() {
        return List.copyOf(entries);
    }

    public record Entry(String action, Long taskId, Instant timestamp) {
    }
}