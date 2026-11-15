package com.example.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskAsyncServiceTest {

    @Mock
    private TaskAsyncWorker worker;

    @InjectMocks
    private TaskAsyncService service;

    @Test
    void notificationIsDispatched() {
        service.notifyTask(15L, false);

        verify(worker).sendNotification(15L, false);
    }

    @Test
    void twoResultsAreCombinedAfterBothComplete() throws Exception {
        CompletableFuture<String> report = new CompletableFuture<>();
        CompletableFuture<String> status = new CompletableFuture<>();

        when(worker.generateReport(15L)).thenReturn(report);
        when(worker.checkExternalStatus(15L, false, false))
                .thenReturn(status);

        CompletableFuture<String> summary =
                service.generateSummary(15L, false, false);

        verify(worker).generateReport(15L);
        verify(worker).checkExternalStatus(15L, false, false);

        assertFalse(summary.isDone());

        report.complete("Report ready");
        assertFalse(summary.isDone());

        status.complete("Status OK");

        assertEquals(
                "Report ready | Status OK",
                summary.get(1, TimeUnit.SECONDS)
        );
    }

    @Test
    void externalFailureUsesFallback() throws Exception {
        when(worker.generateReport(15L))
                .thenReturn(CompletableFuture.completedFuture("Report ready"));

        when(worker.checkExternalStatus(15L, true, false))
                .thenReturn(CompletableFuture.failedFuture(
                        new IllegalStateException("Service unavailable")
                ));

        String result = service.generateSummary(15L, true, false)
                .get(1, TimeUnit.SECONDS);

        assertEquals(
                "Report ready | External status unavailable",
                result
        );
    }

    @Test
    void timeoutUsesFallback() throws Exception {
        when(worker.generateReport(15L))
                .thenReturn(CompletableFuture.completedFuture("Report ready"));

        when(worker.checkExternalStatus(15L, false, true))
                .thenReturn(new CompletableFuture<>());

        String result = service.generateSummary(15L, false, true)
                .get(5, TimeUnit.SECONDS);

        assertEquals(
                "Report ready | External status unavailable",
                result
        );
    }
}