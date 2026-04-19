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
package cn.nm.lms.carpetlmsaddition.rule.util.storage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jspecify.annotations.Nullable;

import cn.nm.lms.carpetlmsaddition.Mod;
import cn.nm.lms.carpetlmsaddition.lib.AsyncTasks;
import cn.nm.lms.carpetlmsaddition.lib.getvalue.GetPaths;

public class GenerateResponse {
    private static final Path storageDataPath = GetPaths.getLmsWorldDataPath().resolve("checkStorageData");

    public static CompletableFuture<String> generateJsonResponseAsync() {
        return AsyncTasks.supply(() -> {
            JsonArray storageList = readStorageListFromConfig();
            if (storageList == null) {
                return "[]";
            }

            JsonArray result = buildResponseArray(storageList);
            if (result != null) {
                return result.toString();
            }

            Storage.checkStorage();
            result = buildResponseArray(storageList);
            if (result != null) {
                return result.toString();
            }

            return "[]";
        });
    }

    private static @Nullable JsonArray readStorageListFromConfig() {
        try {
            JsonObject config = JsonParser.parseString(Files.readString(Storage.configJsonPath)).getAsJsonObject();
            if (!config.has("storageList") || !config.get("storageList").isJsonArray()) {
                Mod.LOGGER.warn("Missing or invalid storageList in {}", Storage.configJsonPath);
                return null;
            }
            return config.getAsJsonArray("storageList");
        } catch (Exception e) {
            Mod.LOGGER.warn("Failed to read storageList config: {}", Storage.configJsonPath);
            return null;
        }
    }

    private static @Nullable JsonArray buildResponseArray(JsonArray storageList) {
        JsonArray response = new JsonArray();
        for (JsonElement element : storageList) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                Mod.LOGGER.warn("Skip invalid storage entry: {}", element);
                return null;
            }

            String storageFileName = element.getAsString();
            Path dataFile = storageDataPath.resolve(storageFileName);
            if (!Files.exists(dataFile)) {
                Mod.LOGGER.warn("Storage data file not found: {}", dataFile);
                return null;
            }

            try {
                JsonElement data = JsonParser.parseString(Files.readString(dataFile));
                JsonObject oneStorage = new JsonObject();
                String name = storageFileName.endsWith(".json")
                    ? storageFileName.substring(0, storageFileName.length() - 5) : storageFileName;
                oneStorage.addProperty("name", name);
                oneStorage.add("data", data);
                response.add(oneStorage);
            } catch (Exception e) {
                Mod.LOGGER.warn("Failed to read storage data file: {}", dataFile);
                return null;
            }
        }
        return response;
    }
}
