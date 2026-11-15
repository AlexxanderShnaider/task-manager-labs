package com.example.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

@Component
public class TaskTimingMetrics {

  private final ConcurrentMap<String, AtomicInteger> counts = new ConcurrentHashMap<>();

  private final ConcurrentMap<String, AtomicLong> durations = new ConcurrentHashMap<>();

  public void record(String method, long durationNanos) {
    counts.computeIfAbsent(method, key -> new AtomicInteger())
        .incrementAndGet();

    durations.computeIfAbsent(method, key -> new AtomicLong())
        .set(Math.max(0, durationNanos));
  }

  public int getCount(String method) {
    AtomicInteger count = counts.get(method);
    return count == null ? 0 : count.get();
  }

  public long getLastDurationNanos(String method) {
    AtomicLong duration = durations.get(method);
    return duration == null ? 0 : duration.get();
  }

  public void clear() {
    counts.clear();
    durations.clear();
  }
}