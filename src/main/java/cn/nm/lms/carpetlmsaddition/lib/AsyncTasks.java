/*
 * Copyright (C) 2025  Carpet-LMS-Addition contributors
 * https://github.com/Citrus-Union/Carpet-LMS-Addition

 * Carpet LMS Addition is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.

 * Carpet LMS Addition is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Carpet LMS Addition.  If not, see <https://www.gnu.org/licenses/>.
 */
package cn.nm.lms.carpetlmsaddition.lib;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import cn.nm.lms.carpetlmsaddition.Mod;

public final class AsyncTasks {
    private static volatile ExecutorService executor;

    private AsyncTasks() {}

    public static synchronized void init() {
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newCachedThreadPool(namedDaemonFactory(Mod.COMPACT_NAME));
        }
    }

    public static <T> CompletableFuture<T> supply(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, requireExecutor());
    }

    public static CompletableFuture<Void> run(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, requireExecutor());
    }

    public static ExecutorService executor() {
        return requireExecutor();
    }

    public static synchronized void shutdown() {
        ExecutorService current = executor;
        if (current == null) {
            return;
        }
        current.shutdownNow();
        executor = null;
    }

    private static ExecutorService requireExecutor() {
        ExecutorService current = executor;
        if (current == null || current.isShutdown()) {
            throw new IllegalStateException("AsyncTasks is not initialized for current server lifecycle");
        }
        return current;
    }

    private static ThreadFactory namedDaemonFactory(String namePrefix) {
        AtomicInteger id = new AtomicInteger(1);
        return runnable -> {
            Thread thread = new Thread(runnable, namePrefix + "-" + id.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };
    }
}
