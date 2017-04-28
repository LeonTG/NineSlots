/*
 * Project: NineSlots
 * Class: com.leontg77.nineslots.commands.TeamInvCommand
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Leon Vaktskjold <leontg77@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.leontg77.nineslots.commands;

import com.google.common.collect.Lists;
import com.leontg77.nineslots.Main;
import com.leontg77.nineslots.listeners.ClickListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.StringUtil;

import java.util.List;

/**
 * NineSlots command class.
 *
 * @author LeonTG
 */
public class NineSlotsCommand implements CommandExecutor, TabCompleter {
    private static final String PERMISSION = "nineslots.manage";

    private final ClickListener listener;
    private final Main plugin;

    public NineSlotsCommand(Main plugin, ClickListener listener) {
        this.listener = listener;
        this.plugin = plugin;
    }

    private BukkitRunnable task = null;
    private boolean enabled = false;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Main.PREFIX + "Usage: /nineslots <info|enable|disable>");
            return true;
        }

        if (args[0].equalsIgnoreCase("info")) {
            sender.sendMessage(Main.PREFIX + "Plugin creator: §aLeonTG");
            sender.sendMessage(Main.PREFIX + "Version: §a" + plugin.getDescription().getVersion());
            sender.sendMessage(Main.PREFIX + "Description:");
            sender.sendMessage("§8» §f" + plugin.getDescription().getDescription());
            return true;
        }

        if (args[0].equalsIgnoreCase("enable")) {
            if (!sender.hasPermission(PERMISSION)) {
                sender.sendMessage(ChatColor.RED + "You don't have permission.");
                return true;
            }

            if (enabled) {
                sender.sendMessage(Main.PREFIX + "Nine Slots is already enabled.");
                return true;
            }

            plugin.broadcast(Main.PREFIX + "Nine Slots has been enabled.");
            enabled = true;

            Bukkit.getPluginManager().registerEvents(listener, plugin);

            task = new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        for (int i = 9; i < 36; i++) {
                            online.getInventory().setItem(i, Main.BLOCKED_ITEM);
                        }
                    }
                }
            };

            task.runTaskTimer(plugin, 0L, 100L);
            return true;
        }

        if (args[0].equalsIgnoreCase("disable")) {
            if (!sender.hasPermission(PERMISSION)) {
                sender.sendMessage(ChatColor.RED + "You don't have permission.");
                return true;
            }

            if (!enabled) {
                sender.sendMessage(Main.PREFIX + "Nine Slots is not enabled.");
                return true;
            }

            plugin.broadcast(Main.PREFIX + "Nine Slots has been disabled.");
            enabled = false;

            HandlerList.unregisterAll(listener);

            if (task != null) {
                task.cancel();
            }

            task = null;
            return true;
        }

        sender.sendMessage(Main.PREFIX + "Usage: /nineslots <info|enable|disable>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> list = Lists.newArrayList();

        if (args.length != 1) {
            return Lists.newArrayList();
        }

        list.add("info");

        if (sender.hasPermission(PERMISSION)) {
            list.add("enable");
            list.add("disable");
        }

        return StringUtil.copyPartialMatches(args[args.length - 1], list, Lists.newArrayList());
    }
}