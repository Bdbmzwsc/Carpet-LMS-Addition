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

import java.io.IOException;
import java.nio.file.Path;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cn.nm.lms.carpetlmsaddition.lib.JsonFileIo;
import cn.nm.lms.carpetlmsaddition.lib.getvalue.GetPaths;

final class UserPasswordStore {
    private static final Path USERS_PATH = GetPaths.getLmsConfigSecretPath().resolve("users.json");

    private UserPasswordStore() {}

    static synchronized void setPasswordHash(String username, String passwordHash) throws UserDataException {
        try {
            JsonFileIo.putString(USERS_PATH, passwordHash, username);
        } catch (IOException e) {
            throw new UserDataException("Failed to write users data", e);
        }
    }

    static synchronized String getPasswordHash(String username) throws UserDataException {
        JsonObject users = readUsers();
        JsonElement passwordHash = users.get(username);
        if (passwordHash == null || passwordHash.isJsonNull()) {
            return null;
        }
        if (!passwordHash.isJsonPrimitive() || !passwordHash.getAsJsonPrimitive().isString()) {
            throw new UserDataException("Invalid user password data");
        }
        String value = passwordHash.getAsString();
        return value.isEmpty() ? null : value;
    }

    private static JsonObject readUsers() throws UserDataException {
        try {
            return JsonFileIo.readObjectOrEmpty(USERS_PATH);
        } catch (IOException e) {
            throw new UserDataException("Invalid users data", e);
        }
    }

    static final class UserDataException extends Exception {
        UserDataException(String message) {
            super(message);
        }

        UserDataException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
