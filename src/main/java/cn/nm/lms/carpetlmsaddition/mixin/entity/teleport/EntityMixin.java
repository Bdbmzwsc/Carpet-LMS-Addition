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
package cn.nm.lms.carpetlmsaddition.mixin.entity.teleport;

import java.util.Set;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import cn.nm.lms.carpetlmsaddition.rule.Settings;

// <=1.21.1:
// versions/1.21.1/src/main/java/cn/nm/lms/carpetlmsaddition/mixin/entity/teleport/EntityMixin.java
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Unique
    private void teleportSetPosition$lms(boolean isHighVersion, Entity entity3,
        PositionMoveRotation positionMoveRotation2, Set<Relative> set) {
        Entity base = (Entity)(Object)this;
        Entity target = isHighVersion ? base : entity3;
        PositionMoveRotation positionMoveRotation3 =
            PositionMoveRotation.calculateAbsolute(PositionMoveRotation.of(target), positionMoveRotation2, set);
        entity3.setPosRaw(positionMoveRotation3.position().x, positionMoveRotation3.position().y,
            positionMoveRotation3.position().z);
        entity3.setYRot(positionMoveRotation3.yRot());
        entity3.setYHeadRot(positionMoveRotation3.yRot());
        entity3.setXRot(positionMoveRotation3.xRot());
        entity3.reapplyPosition();
        entity3.setOldPosAndRot();
        entity3.setDeltaMovement(positionMoveRotation3.deltaMovement());
        //#if MC>=1.21.6
        entity3.clearMovementThisTick();
        //#else
        //$$ entity3.movementThisTick.clear();
        //#endif
    }

    @WrapOperation(method = "teleportCrossDimension",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;teleportSetPosition("
            //#if MC>=12109
            + "Lnet/minecraft/world/entity/PositionMoveRotation;"
            //#endif
            + "Lnet/minecraft/world/entity/PositionMoveRotation;" + "Ljava/util/Set;)V"))
    private void teleportSetPositionWrap$lms(Entity entity3,
        //#if MC>=12109
        PositionMoveRotation positionMoveRotation,
        //#endif
        PositionMoveRotation positionMoveRotation2, Set<Relative> set, Operation<Void> original) {
        Boolean isHighVersion = switch (Settings.entityTeleportCrossDimension) {
            case "1.21.9+" -> true;
            case "1.21.8-" -> false;
            default -> null;
        };
        if (isHighVersion == null) {
            original.call(entity3,
                //#if MC>=12109
                positionMoveRotation,
                //#endif
                positionMoveRotation2, set);
            return;
        }
        this.teleportSetPosition$lms(isHighVersion, entity3, positionMoveRotation2, set);
    }
}