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
package cn.nm.lms.carpetlmsaddition.rule.block.dispenser.bartering;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;

import cn.nm.lms.carpetlmsaddition.lib.Utils;

final class DispenserBarterInputParser {
    private int barterRollCount;
    private final List<ItemStack> shulkerBoxesToDrop;
    private boolean valid;

    private DispenserBarterInputParser() {
        this.barterRollCount = 0;
        this.shulkerBoxesToDrop = new ArrayList<>();
        this.valid = true;
    }

    static ParseResult parseBarterInput(ItemStack input) {
        DispenserBarterInputParser parser = new DispenserBarterInputParser();
        parser.parseItemRecursively(input);
        return new ParseResult(parser.barterRollCount, parser.shulkerBoxesToDrop, parser.valid);
    }

    private void parseItemRecursively(ItemStack input) {
        if (!valid || input.isEmpty()) {
            return;
        }

        if (input.is(Items.GOLD_INGOT)) {
            this.barterRollCount += input.getCount();
            return;
        }
        if (input.is(Items.GOLD_BLOCK)) {
            this.barterRollCount += input.getCount() * 9;
            return;
        }
        if (Utils.isShulkerBox(input)) {
            int shulkerCount = input.getCount();
            this.shulkerBoxesToDrop.add(input.copyWithCount(shulkerCount));

            ItemContainerContents container = input.get(DataComponents.CONTAINER);
            if (container == null) {
                return;
            }
            for (ItemStack inner :
            //#if MC>=260100
            container.nonEmptyItemCopyStream().toList()
            //#else
            //$$ container.nonEmptyItemsCopy()
            //#endif
            ) {
                this.parseItemRecursively(inner.copyWithCount(inner.getCount() * shulkerCount));
                if (!valid) {
                    return;
                }
            }
            return;
        }
        this.valid = false;
    }

    record ParseResult(int barterRollCount, List<ItemStack> shulkerBoxesToDrop, boolean valid) {
    }
}
