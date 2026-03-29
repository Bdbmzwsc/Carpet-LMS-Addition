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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import carpet.CarpetServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cn.nm.lms.carpetlmsaddition.CarpetLMSAdditionMod;

public final class PlayerConfig
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String PLAYER_CONFIG_KEY = "playerConfig";
    private static JsonObject root;
    private static Path loadedFile;

    public static String get(UUID playerUUID, String configName)
    {
        JsonObject data = ensureLoaded();
        JsonObject allConfig = data.getAsJsonObject(PLAYER_CONFIG_KEY);
        if (allConfig == null) return null;
        JsonObject perConfig = allConfig.getAsJsonObject(configName);
        if (perConfig == null) return null;
        return perConfig.get(playerUUID.toString()) != null ? perConfig.get(playerUUID.toString())
                                                                       .getAsString() : null;
    }

    public static void set(UUID playerUUID, String configName, String value)
    {
        Path file = currentFile();
        JsonObject data = ensureLoaded();
        JsonObject allConfig = data.getAsJsonObject(PLAYER_CONFIG_KEY);
        if (allConfig == null)
        {
            allConfig = new JsonObject();
        }
        JsonObject perConfig = allConfig.getAsJsonObject(configName);
        if (perConfig == null)
        {
            perConfig = new JsonObject();
        }
        perConfig.addProperty(playerUUID.toString(), value);
        allConfig.add(configName, perConfig);
        data.add(PLAYER_CONFIG_KEY, allConfig);
        try
        {
            Files.createDirectories(file.getParent());
            try (
                    BufferedWriter writer = Files.newBufferedWriter(file)
            )
            {
                GSON.toJson(data, writer);
            }
            loadedFile = file;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static JsonObject ensureLoaded()
    {
        Path file = currentFile();
        if (root != null && file.equals(loadedFile)) return root;
        if (Files.exists(file))
        {
            try (
                    BufferedReader reader = Files.newBufferedReader(file)
            )
            {
                root = JsonParser.parseReader(reader).getAsJsonObject();
                loadedFile = file;
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            root = new JsonObject();
            loadedFile = file;
        }
        return root;
    }

    private static Path currentFile()
    {
        MinecraftServer server = CarpetServer.minecraft_server;
        return Objects.requireNonNull(server, "Minecraft server not ready")
                      .getWorldPath(LevelResource.ROOT)
                      .resolve(CarpetLMSAdditionMod.COMPACT_NAME)
                      .resolve("config.json");
    }
}
