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
package cn.nm.lms.carpetlmsaddition.bot;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;

import carpet.CarpetServer;
import carpet.fakes.ServerPlayerInterface;
import carpet.patches.EntityPlayerMPFake;

import cn.nm.lms.carpetlmsaddition.lib.PlayerUtils;
import cn.nm.lms.carpetlmsaddition.lib.Utils;
import cn.nm.lms.carpetlmsaddition.lib.getvalue.GetPaths;

public final class CleanGetItemBot {
    private CleanGetItemBot() {}

    public static List<String> listUnspawnedGetItemBotsWithInventory() {
        return listBots();
    }

    public static List<String> listBots() {
        MinecraftServer server = CarpetServer.minecraft_server;
        if (server == null) {
            return List.of();
        }

        String prefix = GetItemBotHelper.getBotPrefix();
        Path playerDataDir = GetPaths.getWorldPath(LevelResource.PLAYER_DATA_DIR);
        Set<String> onlineNames = Utils.runOnServerThread(server, () -> server.getPlayerList().getPlayers().stream()
            .map(ServerPlayer::getScoreboardName).collect(Collectors.toSet()));

        return IntStream.rangeClosed(1, GetItemBotHelper.BOT_SCAN_LIMIT).parallel().mapToObj(i -> prefix + i)
            .filter(name -> !onlineNames.contains(name)).filter(name -> hasOfflineItems(name, playerDataDir)).toList();
    }

    private static boolean hasOfflineData(String playerName, Path playerDataDir) {
        UUID uuid = UUIDUtil.createOfflinePlayerUUID(playerName);
        Path playerDat = playerDataDir.resolve(uuid.toString() + ".dat");
        return Files.isRegularFile(playerDat);
    }

    private static boolean hasOfflineItems(String playerName, Path playerDataDir) {
        if (!hasOfflineData(playerName, playerDataDir)) {
            return false;
        }
        UUID uuid = UUIDUtil.createOfflinePlayerUUID(playerName);
        Path playerDat = playerDataDir.resolve(uuid.toString() + ".dat");
        try {
            return !PlayerUtils.isMainInvAndHotbarEmpty(playerDat);
        } catch (Exception e) {
            return false;
        }
    }

    public static List<String> cleanBots(ServerPlayer player, int max) {
        SpawnData spawnData = new SpawnData(player);
        List<String> bots = listBots();
        List<String> result = new ArrayList<>();
        for (String botName : bots) {
            if (max <= 0) {
                return result;
            }
            max -= cleanSingleBot(spawnData, botName, max);
            result.add(botName);
        }
        return result;
    }

    private static int cleanSingleBot(SpawnData spawnData, String name, int max) {
        if (max <= 0) {
            return 0;
        }
        EntityPlayerMPFake bot = spawnData.spawn(name);
        try {
            return Utils.runOnServerThread(spawnData.server, () -> {
                int dropped = 0;
                ServerPlayerInterface playerInterface = (ServerPlayerInterface)bot;
                for (int slot = 0; slot < 36 && dropped < max; slot++) {
                    if (bot.getInventory().getItem(slot).isEmpty()) {
                        continue;
                    }
                    playerInterface.getActionPack().drop(slot, true);
                    dropped++;
                }
                return dropped;
            });
        } finally {
            FakePlayerSpawner.silenceLogout(bot);
        }
    }

    static class SpawnData {
        Vec3 spawnPos;
        float yaw;
        float pitch;
        MinecraftServer server;
        ResourceKey<Level> dimension;

        SpawnData(ServerPlayer player) {
            this.spawnPos = player.position();
            this.yaw = player.getYRot();
            this.pitch = player.getXRot();
            this.server = player.level().getServer();
            this.dimension = player.level().dimension();
        }

        EntityPlayerMPFake spawn(String name) {
            return FakePlayerSpawner.spawnSurvivalFakeWithName(this.server, name, this.dimension, spawnPos, yaw, pitch,
                true);
        }
    }
}
