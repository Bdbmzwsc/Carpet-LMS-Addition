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
package cn.nm.lms.carpetlmsaddition.mixin.recipe;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import cn.nm.lms.carpetlmsaddition.rule.recipe.runtime.RecipeRuleHelper;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
    @Inject(method = "prepare", at = @At("RETURN"), cancellable = true)
    private void injectLmsRecipes$lms(ResourceManager resourceManager, ProfilerFiller profiler,
        CallbackInfoReturnable<RecipeMap> cir) {
        RecipeMap original = cir.getReturnValue();
        Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> merged = new LinkedHashMap<>();
        for (RecipeHolder<?> recipeHolder : original.values()) {
            merged.put(recipeHolder.id(), recipeHolder);
        }
        for (RecipeHolder<?> recipeHolder : RecipeRuleHelper.getRecipes()) {
            merged.put(recipeHolder.id(), recipeHolder);
        }
        cir.setReturnValue(RecipeMap.create(merged.values()));
    }
}
