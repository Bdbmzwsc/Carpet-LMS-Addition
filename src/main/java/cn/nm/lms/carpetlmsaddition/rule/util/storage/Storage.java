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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import carpet.CarpetServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import cn.nm.lms.carpetlmsaddition.Mod;
import cn.nm.lms.carpetlmsaddition.lib.AsyncTasks;
import cn.nm.lms.carpetlmsaddition.lib.getvalue.GetPaths;

public class Storage {
    static final Path configJsonPath = GetPaths.getLmsWorldPath().resolve("checkStorageConfig.json");
    static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();
    static final Map<String, ResourceKey<Level>> stringToDimension =
        Map.of("end", Level.END, "overworld", Level.OVERWORLD, "nether", Level.NETHER);
    static final Map<ResourceKey<Level>, String> dimensionToSting =
        Map.of(Level.END, "end", Level.OVERWORLD, "overworld", Level.NETHER, "nether");
    static final Path storageDir = GetPaths.getLmsWorldPath().resolve("checkStorageList");
    static final Path storageDataPath = GetPaths.getLmsWorldDataPath().resolve("checkStorageData");

    public static String checkStorage() {
        MinecraftServer server = CarpetServer.minecraft_server;
        if (server.isSameThread()) {
            return doCheckStorage(server);
        }

        CompletableFuture<String> future = new CompletableFuture<>();
        server.execute(() -> {
            try {
                future.complete(doCheckStorage(server));
            } catch (Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        });
        return future.join();
    }

    private static String doCheckStorage(MinecraftServer server) {
        Count count = new Count();
        PreparedInputs prepared = AsyncTasks.supply(StorageJsonService::prepareInputs).join();
        count.total = prepared.total;

        for (PreparedStorage storageInput : prepared.inputs) {
            try {
                HashMap<Item, ItemCount> items = new HashMap<>();
                List<Position> errors = new ArrayList<>();
                List<ContainerSnapshot> snapshots =
                    StorageContainerReader.collectSnapshots(storageInput.pos, server, errors);
                StorageItemStackProcessor.processSnapshots(snapshots, items);
                Path savePath = storageDataPath.resolve(storageInput.fileName);
                StorageJsonService.saveToFile(savePath, items, errors);
                count.success++;
            } catch (Exception e) {
                Mod.LOGGER.warn("Failed to process storage file: {}", storageInput.fileName);
            }
        }

        return count.praseResult();
    }

    static final class Position {
        final ResourceKey<Level> dimension;
        final BlockPos pos;

        Position(ResourceKey<Level> dimension, BlockPos pos) {
            this.dimension = dimension;
            this.pos = pos;
        }

        JsonObject toJson() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("dimension", dimensionToSting.get(this.dimension));
            JsonObject posObject = new JsonObject();
            posObject.addProperty("x", this.pos.getX());
            posObject.addProperty("y", this.pos.getY());
            posObject.addProperty("z", this.pos.getZ());
            jsonObject.add("pos", posObject);
            return jsonObject;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Position other)) {
                return false;
            }
            return Objects.equals(dimension, other.dimension) && Objects.equals(pos, other.pos);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dimension, pos);
        }
    }

    static final class ContainerSnapshot {
        final Position position;
        final List<ItemStack> stacks;

        ContainerSnapshot(Position position, List<ItemStack> stacks) {
            this.position = position;
            this.stacks = stacks;
        }
    }

    static final class PreparedStorage {
        final String fileName;
        final JsonObject pos;

        PreparedStorage(String fileName, JsonObject pos) {
            this.fileName = fileName;
            this.pos = pos;
        }
    }

    static final class PreparedInputs {
        static final PreparedInputs EMPTY = new PreparedInputs(0, List.of());
        final int total;
        final List<PreparedStorage> inputs;

        PreparedInputs(int total, List<PreparedStorage> inputs) {
            this.total = total;
            this.inputs = inputs;
        }
    }

    static final class Count {
        int success = 0;
        int total = 0;

        String praseResult() {
            String text = String.format("CheckStorage finished, success: %d, total: %d", this.success, this.total);
            Mod.LOGGER.info(text);
            return text;
        }
    }

    static final class ItemCount {
        int count = 0;
        HashMap<Position, Integer> positionsCount = new HashMap<>();

        synchronized void addItemStack(ItemStack itemStack, int times, Position position) {
            int increaseCount = itemStack.getCount() * times;
            this.count += increaseCount;
            int newPositionCount = this.positionsCount.getOrDefault(position, 0) + increaseCount;
            this.positionsCount.put(position, newPositionCount);
        }
    }
}
