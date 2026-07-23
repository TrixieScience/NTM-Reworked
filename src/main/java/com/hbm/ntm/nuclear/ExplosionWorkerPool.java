package com.hbm.ntm.nuclear;

import com.hbm.ntm.config.HbmConfig;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

final class ExplosionWorkerPool {
    private static final AtomicInteger THREAD_NUMBER = new AtomicInteger();
    private static volatile ExecutorService executor;
    private static volatile int workers;

    private ExplosionWorkerPool() {
    }

    static ExecutorService executor() {
        ExecutorService current = executor;
        if (current != null) return current;
        synchronized (ExplosionWorkerPool.class) {
            current = executor;
            if (current == null) {
                workers = configuredWorkers();
                ThreadFactory factory = task -> {
                    Thread thread = new Thread(task,
                            "HBM-Explosion-Worker-" + THREAD_NUMBER.incrementAndGet());
                    thread.setDaemon(true);
                    thread.setPriority(Thread.NORM_PRIORITY - 1);
                    return thread;
                };
                current = Executors.newFixedThreadPool(workers, factory);
                executor = current;
            }
        }
        return current;
    }

    static int workers() {
        executor();
        return workers;
    }

    private static int configuredWorkers() {
        int configured = HbmConfig.MK5_BLAST_THREADS.get();
        if (configured > 0) return configured;
        return Math.max(1, Math.min(12, Runtime.getRuntime().availableProcessors() / 2));
    }
}
