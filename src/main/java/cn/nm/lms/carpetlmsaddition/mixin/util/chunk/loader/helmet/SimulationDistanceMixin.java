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
package cn.nm.lms.carpetlmsaddition.mixin.util.chunk.loader.helmet;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.Ticket;
import net.minecraft.world.level.ChunkPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import cn.nm.lms.carpetlmsaddition.lib.getvalue.HelmetLoadValue;

@Mixin(DistanceManager.class)
public abstract class SimulationDistanceMixin {
    @Unique
    private static final Map<UUID, Integer> lastSimTicketLevel = new ConcurrentHashMap<>();

    @Shadow
    private int simulationDistance;

    //#if MC>=12105
    @Redirect(method = "addPlayer(Lnet/minecraft/core/SectionPos;Lnet/minecraft/server/level/ServerPlayer;)V", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/level/TicketStorage;addTicket(Lnet/minecraft/server/level/Ticket;Lnet/minecraft/world/level/ChunkPos;)V"))
    private void applyHelmetSimulationDistanceOnAdd$LMS(net.minecraft.world.level.TicketStorage ticketStorage,
        Ticket ticket, ChunkPos chunkPos, SectionPos sectionPos, ServerPlayer serverPlayer) {
        int adjustedLevel = this.createSimulationTicketLevel$LMS(ticket.getTicketLevel(), serverPlayer);
        lastSimTicketLevel.put(serverPlayer.getUUID(), adjustedLevel);
        if (adjustedLevel == ticket.getTicketLevel()) {
            ticketStorage.addTicket(ticket, chunkPos);
            return;
        }
        ticketStorage.addTicket(new Ticket(ticket.getType(), adjustedLevel), chunkPos);
    }
    //#else
    //$$ @Redirect(method = "addPlayer(Lnet/minecraft/core/SectionPos;Lnet/minecraft/server/level/ServerPlayer;)V", at = @At(
    //$$     value = "INVOKE",
    //$$     target = "Lnet/minecraft/server/level/TickingTracker;addTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"))
    //$$ @SuppressWarnings({ "rawtypes", "unchecked" })
    //$$ private void applyHelmetSimulationDistanceOnAdd$LMS(net.minecraft.server.level.TickingTracker tickingTracker,
    //$$     net.minecraft.server.level.TicketType ticketType, ChunkPos chunkPos, int ticketLevel, Object key,
    //$$     SectionPos sectionPos, ServerPlayer serverPlayer) {
    //$$     int adjustedLevel = this.createSimulationTicketLevel$LMS(ticketLevel, serverPlayer);
    //$$     lastSimTicketLevel.put(serverPlayer.getUUID(), adjustedLevel);
    //$$     tickingTracker.addTicket(ticketType, chunkPos, adjustedLevel, key);
    //$$ }
    //#endif

    //#if MC>=12105
    @Redirect(method = "removePlayer(Lnet/minecraft/core/SectionPos;Lnet/minecraft/server/level/ServerPlayer;)V",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/TicketStorage;removeTicket(Lnet/minecraft/server/level/Ticket;Lnet/minecraft/world/level/ChunkPos;)V"))
    private void applyHelmetSimulationDistanceOnRemove$LMS(net.minecraft.world.level.TicketStorage ticketStorage,
        Ticket ticket, ChunkPos chunkPos, SectionPos sectionPos, ServerPlayer serverPlayer) {
        int adjustedLevel = this.createSimulationTicketLevel$LMS(ticket.getTicketLevel(), serverPlayer);
        long chunkKey$LMS = this.chunkKey$LMS(chunkPos);
        Integer storedLevel = lastSimTicketLevel.remove(serverPlayer.getUUID());

        if (storedLevel != null && storedLevel != ticket.getTicketLevel()) {
            Ticket storedTicket = new Ticket(ticket.getType(), storedLevel);
            if (ticketStorage.removeTicket(chunkKey$LMS, storedTicket)) {
                return;
            }
        }

        if (adjustedLevel != ticket.getTicketLevel()) {
            Ticket adjustedTicket = new Ticket(ticket.getType(), adjustedLevel);
            if (ticketStorage.removeTicket(chunkKey$LMS, adjustedTicket)) {
                return;
            }
        }

        ticketStorage.removeTicket(chunkKey$LMS, ticket);
    }
    //#else
    //$$ @Redirect(method = "removePlayer(Lnet/minecraft/core/SectionPos;Lnet/minecraft/server/level/ServerPlayer;)V",
    //$$     at = @At(value = "INVOKE",
    //$$         target = "Lnet/minecraft/server/level/TickingTracker;removeTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"))
    //$$ @SuppressWarnings({ "rawtypes", "unchecked" })
    //$$ private void applyHelmetSimulationDistanceOnRemove$LMS(net.minecraft.server.level.TickingTracker tickingTracker,
    //$$     net.minecraft.server.level.TicketType ticketType, ChunkPos chunkPos, int ticketLevel, Object key,
    //$$     SectionPos sectionPos, ServerPlayer serverPlayer) {
    //$$     Integer storedLevel = lastSimTicketLevel.remove(serverPlayer.getUUID());
    //$$     int adjustedLevel = this.createSimulationTicketLevel$LMS(ticketLevel, serverPlayer);
    //$$
    //$$     if (storedLevel != null && storedLevel != ticketLevel) {
    //$$         tickingTracker.removeTicket(ticketType, chunkPos, storedLevel, key);
    //$$     }
    //$$     if (adjustedLevel != ticketLevel) {
    //$$         tickingTracker.removeTicket(ticketType, chunkPos, adjustedLevel, key);
    //$$     }
    //$$
    //$$     tickingTracker.removeTicket(ticketType, chunkPos, ticketLevel, key);
    //$$ }
    //#endif

    @Unique
    private int createSimulationTicketLevel$LMS(int originalLevel, ServerPlayer player) {
        int helmetDistance = HelmetLoadValue.helmetLoadValue(player);
        if (helmetDistance <= 0) {
            return originalLevel;
        }

        int cappedDistance = Math.min(helmetDistance, this.simulationDistance);
        return Math.max(0, ChunkLevel.byStatus(FullChunkStatus.ENTITY_TICKING) - cappedDistance);
    }

    @Unique
    private long chunkKey$LMS(ChunkPos chunkPos) {
        //#if MC>=260100
        return chunkPos.pack();
        //#else
        //$$ return chunkPos.toLong();
        //#endif
    }
}
