package com.example.service;

import com.example.config.AsyncConfig;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = {
        AsyncConfig.class,
        AsyncExecutionTest.ProbeWorker.class
})
class AsyncExecutionTest {

    @Autowired
    private ProbeWorker worker;

    @Test
    void twoTasksRunConcurrentlyInCustomExecutor() throws Exception {
        CountDownLatch started = new CountDownLatch(2);
        CountDownLatch release = new CountDownLatch(1);

        CompletableFuture<String> first =
                worker.run(started, release);

        CompletableFuture<String> second =
                worker.run(started, release);

        try {
            assertTrue(started.await(3, TimeUnit.SECONDS));

            assertFalse(first.isDone());
            assertFalse(second.isDone());
        } finally {
            release.countDown();
        }

        assertTrue(first.get(3, TimeUnit.SECONDS)
                .startsWith("task-async-"));

        assertTrue(second.get(3, TimeUnit.SECONDS)
                .startsWith("task-async-"));
    }

    @Test
    void fireAndForgetDoesNotBlockCaller() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        try {
            assertTimeoutPreemptively(
                    Duration.ofSeconds(1),
                    () -> worker.runWithoutResult(started, release)
            );

            assertTrue(started.await(3, TimeUnit.SECONDS));
        } finally {
            release.countDown();
        }
    }

    @Service
    public static class ProbeWorker {

        @Async("taskAsyncExecutor")
        public CompletableFuture<String> run(
                CountDownLatch started,
                CountDownLatch release) {

            started.countDown();
            awaitRelease(release);

            return CompletableFuture.completedFuture(
                    Thread.currentThread().getName()
            );
        }

        @Async("taskAsyncExecutor")
        public void runWithoutResult(
                CountDownLatch started,
                CountDownLatch release) {

            started.countDown();
            awaitRelease(release);
        }

        private void awaitRelease(CountDownLatch release) {
            try {
                if (!release.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Test task timed out");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
        }
    }
}