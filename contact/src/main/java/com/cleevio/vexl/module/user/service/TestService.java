package com.cleevio.vexl.module.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class TestService {
    private final AtomicInteger atomicNumberOfThreads
            = new AtomicInteger(0);

    @Async("sendNotificationToContactsExecutor")
    public void runDummyAsyncThread() {
        try{
            var numberOfThreads = atomicNumberOfThreads.incrementAndGet();
            log.info("Starting dummy async already have {} threads", numberOfThreads);
            Thread.sleep(10_000);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("Dummy async thread failed...");
        } finally {
            var numberOfThreads = atomicNumberOfThreads.decrementAndGet();
            log.info("Ending dummy async already have {} threads", numberOfThreads);
        }
    }
}
