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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.jspecify.annotations.Nullable;

import cn.nm.lms.carpetlmsaddition.Mod;

final class StorageContainerReader {
    private StorageContainerReader() {}

    static List<Storage.ContainerSnapshot> collectSnapshots(JsonObject object, MinecraftServer server,
        List<Storage.Position> errors) {
        List<Storage.ContainerSnapshot> containerSnapshots = new ArrayList<>();
        Storage.stringToDimension.forEach((dimensionName, dimension) -> {
            if (!object.has(dimensionName) || !object.get(dimensionName).isJsonArray()) {
                Mod.LOGGER.warn("Missing or invalid dimension array: {}", dimensionName);
                return;
            }

            ServerLevel level = server.getLevel(dimension);
            if (level == null) {
                Mod.LOGGER.warn("Invalid dimension level: {}", dimension);
                return;
            }

            oneDimentionfromJson(object.getAsJsonArray(dimensionName), level, dimension, errors, containerSnapshots);
        });

        return containerSnapshots;
    }

    private static void oneDimentionfromJson(JsonArray array, ServerLevel level, ResourceKey<Level> dimension,
        List<Storage.Position> errors, List<Storage.ContainerSnapshot> containerSnapshots) {
        for (int i = 0; i < array.size(); i++) {
            JsonElement element = array.get(i);

            BlockPos pos = parseBlockPos(element);
            if (pos == null) {
                Mod.LOGGER.warn("Invalid array at {}: {}", Storage.dimensionToSting.get(dimension),
                    Objects.toString(element, ""));
                continue;
            }

            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof Container container)) {
                errors.add(new Storage.Position(dimension, pos));
                continue;
            }

            Storage.Position position = new Storage.Position(dimension, pos);
            snapshotOneContainer(container, position, containerSnapshots);
        }
    }

    private static void snapshotOneContainer(Container container, Storage.Position position,
        List<Storage.ContainerSnapshot> snapshots) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            stacks.add(stack.copy());
        }
        snapshots.add(new Storage.ContainerSnapshot(position, stacks));
    }

    private static @Nullable BlockPos parseBlockPos(JsonElement element) {
        if (!element.isJsonArray()) {
            return null;
        }
        JsonArray array = element.getAsJsonArray();
        if (array.size() != 3) {
            return null;
        }
        int x;
        int y;
        int z;
        try {
            x = array.get(0).getAsInt();
            y = array.get(1).getAsInt();
            z = array.get(2).getAsInt();
        } catch (Exception e) {
            return null;
        }

        return new BlockPos(x, y, z);
    }
}
