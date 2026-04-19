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
package cn.nm.lms.carpetlmsaddition.safety;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import carpet.utils.CommandHelper;

import cn.nm.lms.carpetlmsaddition.rule.Settings;
import cn.nm.lms.carpetlmsaddition.rule.util.command.BaseCommand;

public class CommandSetPassword implements BaseCommand {
    private static int setPassword(String password, CommandSourceStack src) {
        ServerPlayer player = src.getPlayer();
        if (player == null) {
            src.sendFailure(Component.literal("Should be executed by a player"));
            return 0;
        }
        String username = player.getName().getString();
        Password.setPasswordAsync(password, username)
            .whenComplete((result, throwable) -> src.getServer().execute(() -> {
                if (throwable != null) {
                    src.sendFailure(Component.literal("Unknown error"));
                    return;
                }
                if (result.isSuccess()) {
                    src.sendSuccess(() -> Component.literal("Set password successfully"), false);
                    return;
                }
                src.sendFailure(Component.literal(result.getMessage()));
            }));
        return 1;
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("setPassword")
            .requires(src -> CommandHelper.canUseCommand(src, Settings.commandSetPassword))
            .then(Commands.argument("password", StringArgumentType.greedyString()).executes(context -> {
                String password = StringArgumentType.getString(context, "password");
                CommandSourceStack src = context.getSource();

                return setPassword(password, src);
            })));

    }

}
