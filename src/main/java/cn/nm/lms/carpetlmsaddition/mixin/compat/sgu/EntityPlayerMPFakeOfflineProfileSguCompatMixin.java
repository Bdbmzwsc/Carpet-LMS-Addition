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
package cn.nm.lms.carpetlmsaddition.mixin.compat.sgu;

import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import carpet.patches.EntityPlayerMPFake;

import cn.nm.lms.carpetlmsaddition.bot.FakePlayerSpawner;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;

@Restriction(require = @Condition("carpet-sgu-addition"))
@Mixin(value = EntityPlayerMPFake.class, priority = 1500, remap = false)
public class EntityPlayerMPFakeOfflineProfileSguCompatMixin {
    @Dynamic("Method betterCreateFake is added by carpet-sgu-addition")
    @WrapOperation(method = "betterCreateFake", remap = false, at = @At(value = "INVOKE", remap = false,
        target = "Lnet/minecraft/server/players/OldUsersConverter;convertMobOwnerIfNecessary(Lnet/minecraft/server/MinecraftServer;Ljava/lang/String;)Ljava/util/UUID;"))
    private static UUID carpetlmsaddition$skipOnlineLookupForOfflineProfile(MinecraftServer server, String username,
        Operation<UUID> original) {
        if (!FakePlayerSpawner.shouldForceOfflineProfile(username)) {
            return original.call(server, username);
        }
        return UUIDUtil.createOfflinePlayerUUID(username);
    }
}
