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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import cn.nm.lms.carpetlmsaddition.lib.Utils;

final class StorageItemStackProcessor {
    private final Map<Item, Storage.ItemCount> items;

    private StorageItemStackProcessor(Map<Item, Storage.ItemCount> items) {
        this.items = items;
    }

    static void processSnapshots(List<Storage.ContainerSnapshot> snapshots, Map<Item, Storage.ItemCount> items) {
        ConcurrentMap<Item, Storage.ItemCount> concurrentItems = new ConcurrentHashMap<>(items);
        StorageItemStackProcessor processor = new StorageItemStackProcessor(concurrentItems);
        snapshots.parallelStream().forEach(snapshot -> {
            processor.addOneContainer(snapshot.stacks, snapshot.position);
        });
        items.clear();
        items.putAll(concurrentItems);
    }

    private void addItemStack(ItemStack itemStack, int times, Storage.Position position) {
        Storage.ItemCount itemCount = this.items.computeIfAbsent(itemStack.getItem(), k -> new Storage.ItemCount());
        itemCount.addItemStack(itemStack, times, position);
    }

    private void praseItemStack(ItemStack itemStack, Storage.Position position) {
        praseItemStack(itemStack, 1, position);
    }

    private void praseItemStack(ItemStack itemStack, int times, Storage.Position position) {
        if (itemStack.isEmpty()) {
            return;
        }
        if (Utils.isShulkerBox(itemStack)) {
            ItemContainerContents container = itemStack.get(DataComponents.CONTAINER);
            if (container == null) {
                this.addItemStack(itemStack, times, position);
                return;
            }
            int shulkerCount = itemStack.getCount();
            List<ItemStack> innerList = Utils.nonItemCopyList(container);
            if (innerList.isEmpty()) {
                this.addItemStack(itemStack, times, position);
                return;
            }
            innerList.forEach(inner -> {
                this.praseItemStack(inner, times * shulkerCount, position);
            });
            return;
        }
        this.addItemStack(itemStack, times, position);
    }

    private void addOneContainer(List<ItemStack> stacks, Storage.Position position) {
        for (ItemStack stack : stacks) {
            this.praseItemStack(stack, position);
        }
    }
}
