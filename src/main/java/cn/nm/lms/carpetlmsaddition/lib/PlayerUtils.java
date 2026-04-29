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
import java.nio.file.Path;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;

public final class PlayerUtils {
    private PlayerUtils() {}

    public static boolean isInventoryEmpty(Path playerDat) throws IOException {
        CompoundTag tag = NbtIo.readCompressed(playerDat, NbtAccounter.unlimitedHeap());
        return isInventoryEmpty(tag);
    }

    public static boolean isInventoryEmpty(CompoundTag tag) {
        return !hasInventoryItem(tag) && !hasEquipmentItem(tag);
    }

    private static boolean hasInventoryItem(CompoundTag tag) {
        //#if MC>=12105
        ListTag inv = tag.getList("Inventory").orElse(new ListTag());
        //#else
        //$$ ListTag inv = tag.getList("Inventory", net.minecraft.nbt.Tag.TAG_COMPOUND);
        //#endif
        for (int i = 0; i < inv.size(); i++) {
            //#if MC>=12105
            CompoundTag one = inv.getCompound(i).orElse(new CompoundTag());
            //#else
            //$$ CompoundTag one = inv.getCompound(i);
            //#endif
            if (isItemTag(one)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasEquipmentItem(CompoundTag tag) {
        //#if MC>=12105
        return tag.getCompound("equipment").map(PlayerUtils::hasItemTagRecursive).orElse(false);
        //#else
        //$$ return false;
        //#endif
    }

    private static boolean hasItemTagRecursive(CompoundTag tag) {
        if (isItemTag(tag)) {
            return true;
        }
        //#if MC>=12105
        for (String key : tag.keySet()) {
            if (tag.getCompound(key).map(PlayerUtils::hasItemTagRecursive).orElse(false)) {
                return true;
            }
            ListTag list = tag.getList(key).orElse(new ListTag());
            for (int i = 0; i < list.size(); i++) {
                if (list.getCompound(i).map(PlayerUtils::hasItemTagRecursive).orElse(false)) {
                    return true;
                }
            }
        }
        //#endif
        return false;
    }

    private static boolean isItemTag(CompoundTag tag) {
        //#if MC>=12105
        String itemId = tag.getStringOr("id", "");
        //#else
        //$$ String itemId = tag.getString("id");
        //#endif
        return !itemId.isEmpty() && !"minecraft:air".equals(itemId);
    }
}
