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
package cn.nm.lms.carpetlmsaddition.rule.block.util.dispenserbartering;

import java.util.function.BooleanSupplier;

import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.NonNull;

public class DispenserBarteringBehavior extends DefaultDispenseItemBehavior
{
    private final BooleanSupplier isBarteringEnabled;
    private final DispenseItemBehavior fallbackDispenseBehavior;

    DispenserBarteringBehavior(
            BooleanSupplier isBarteringEnabled,
            DispenseItemBehavior fallbackDispenseBehavior
    )
    {
        this.isBarteringEnabled = isBarteringEnabled;
        this.fallbackDispenseBehavior = fallbackDispenseBehavior;
    }

    @Override
    protected @NonNull ItemStack execute(@NonNull BlockSource source, @NonNull ItemStack stack)
    {
        if (stack.isEmpty())
        {
            return runFallbackDispense(source, stack);
        }
        if (DispenserBarteringExecutor.shouldBlockByDispenserName(source))
        {
            return runFallbackDispense(source, stack);
        }
        if (!isBarteringEnabled.getAsBoolean())
        {
            return stack;
        }

        DispenserBarterInputParser.ParseResult parseResult = DispenserBarterInputParser.parseBarterInput(
                stack.copyWithCount(1)
        );
        if (!parseResult.valid() || parseResult.barterRollCount() <= 0)
        {
            return stack;
        }

        stack.shrink(1);
        for (
            ItemStack shulkerBox : parseResult.shulkerBoxesToDrop()
        )
        {
            DispenserBarteringExecutor.dropEmptyShulkerBox(source, shulkerBox);
        }
        DispenserBarteringExecutor.dropBarterLoot(source, parseResult.barterRollCount());
        return stack;
    }

    private @NonNull ItemStack runFallbackDispense(
            @NonNull BlockSource source,
            @NonNull ItemStack stack
    )
    {
        return fallbackDispenseBehavior.dispense(source, stack);
    }
}
