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
package cn.nm.lms.carpetlmsaddition.safety;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cn.nm.lms.carpetlmsaddition.lib.getvalue.GetPaths;

public class DBUtil {
    private static final int CONNECT_RETRY_TIMES = 2;
    private static final long CONNECT_RETRY_DELAY_MILLIS = 200L;
    private static final Path CONFIG_PATH = GetPaths.getLmsConfigPath().resolve("database.json");

    private static JsonObject readDatabaseConfig() {
        try {
            return JsonParser.parseString(Files.readString(CONFIG_PATH)).getAsJsonObject();
        } catch (Exception e) {
            throw new IllegalStateException("Invalid database config", e);
        }
    }

    private static String readRequiredConfigItem(JsonObject config, String key) {
        if (!config.has(key) || config.get(key).isJsonNull()) {
            throw new IllegalStateException("Missing database config key: " + key);
        }
        String value = config.get(key).getAsString();
        if (value.isEmpty()) {
            throw new IllegalStateException("Database config key cannot be empty: " + key);
        }
        return value;
    }

    public static Connection getConnection() throws SQLConnectionException {
        String url;
        String user;
        String password;
        try {
            JsonObject config = readDatabaseConfig();
            url = readRequiredConfigItem(config, "url");
            user = readRequiredConfigItem(config, "user");
            password = readRequiredConfigItem(config, "password");
        } catch (Exception e) {
            throw new SQLConnectionException("Invalid database config", e);
        }

        SQLException lastSqlException = null;
        for (int attempt = 1; attempt <= CONNECT_RETRY_TIMES; attempt++) {
            try {
                return DriverManager.getConnection(url, user, password);
            } catch (SQLException e) {
                lastSqlException = e;
                if (attempt == CONNECT_RETRY_TIMES || !isRetryableConnectionError(e)) {
                    break;
                }
                sleepBeforeRetry();
            }
        }

        throw new SQLConnectionException("Failed to connect database", lastSqlException);
    }

    private static boolean isRetryableConnectionError(SQLException e) {
        String sqlState = e.getSQLState();
        return sqlState != null && sqlState.startsWith("08");
    }

    private static void sleepBeforeRetry() throws SQLConnectionException {
        try {
            Thread.sleep(CONNECT_RETRY_DELAY_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLConnectionException("Interrupted while retrying database connection", e);
        }
    }

    public static class SQLConnectionException extends RuntimeException {
        public SQLConnectionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
