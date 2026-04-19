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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletionException;

public final class AsyncFileIo {
    private AsyncFileIo() {}

    public static boolean exists(Path path) {
        return AsyncTasks.supply(() -> Files.exists(path)).join();
    }

    public static void createDirectories(Path path) throws IOException {
        runIo(() -> Files.createDirectories(path));
    }

    public static void createParentDirectories(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent == null) {
            return;
        }
        createDirectories(parent);
    }

    public static String readString(Path path) throws IOException {
        return supplyIo(() -> Files.readString(path, StandardCharsets.UTF_8));
    }

    public static void writeString(Path path, String content) throws IOException {
        runIo(() -> {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(path, content, StandardCharsets.UTF_8);
        });
    }

    private static <T> T supplyIo(IoSupplier<T> supplier) throws IOException {
        try {
            return AsyncTasks.supply(() -> {
                try {
                    return supplier.get();
                } catch (IOException e) {
                    throw new CompletionException(e);
                }
            }).join();
        } catch (CompletionException e) {
            throw unwrapIOException(e);
        }
    }

    private static void runIo(IoRunnable runnable) throws IOException {
        try {
            AsyncTasks.run(() -> {
                try {
                    runnable.run();
                } catch (IOException e) {
                    throw new CompletionException(e);
                }
            }).join();
        } catch (CompletionException e) {
            throw unwrapIOException(e);
        }
    }

    private static IOException unwrapIOException(CompletionException e) {
        Throwable cause = e.getCause();
        if (cause instanceof IOException ioException) {
            return ioException;
        }
        if (cause instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        throw new RuntimeException(cause);
    }

    @FunctionalInterface
    private interface IoRunnable {
        void run() throws IOException;
    }

    @FunctionalInterface
    private interface IoSupplier<T> {
        T get() throws IOException;
    }
}
