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
package cn.nm.lms.carpetlmsaddition.rule.recipe.runtime;

import java.util.Collection;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;

public final class RecipeBookHelper {
    public static void syncPlayer(ServerPlayer player) {
        Collection<RecipeHolder<?>> allRecipes = LmsRecipeManager.getAllManagedRecipes();
        if (!allRecipes.isEmpty()) {
            player.resetRecipes(allRecipes);
        }
        Collection<RecipeHolder<?>> enabledRecipes = LmsRecipeManager.getCustomRecipes();
        if (!enabledRecipes.isEmpty()) {
            player.awardRecipes(enabledRecipes);
        }
    }

    public static void syncOnlinePlayers(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            syncPlayer(player);
        }
    }
}
