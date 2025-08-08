package me.hhh.amonplugin.listeners;

import me.hhh.amonplugin.Helpers.PluginHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Criterias;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class HealthBarListener implements Listener {

    private final Plugin plugin;

    public HealthBarListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();
        if(PluginHelper.isH(player)){
            return;
        }
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        ChatColor color = ChatColor.RED;
        if(PluginHelper.isOnlinePlayer(plugin.getServer(), player)){
            color = ChatColor.GREEN;
        }
        Objective objective = board.registerNewObjective("HealthBar", Criterias.HEALTH, color + "❤");
        objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        player.setScoreboard(board);
    }
}
