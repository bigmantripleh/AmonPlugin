package me.hhh.amonplugin.commands;

import me.hhh.amonplugin.Main;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;

public class TPSelectedEntity implements CommandExecutor, Listener {

    private Main plugin;

    public TPSelectedEntity(Main plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(plugin.tptarget==null)
        {
            commandSender.sendMessage("Target null");
            return false;
        }
        if(commandSender instanceof Player)
        {
            commandSender.sendMessage("Entity teleported to your location!");
            Player player = (Player) commandSender;
            plugin.tptarget.teleport(player);
            return true;
        }
        return false;
    }

    @EventHandler
    public void selectEntity(PlayerInteractEntityEvent event)
    {
        Player player = event.getPlayer();
        Entity ent = event.getRightClicked();

        if(!event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.STICK)||!event.getPlayer().getName().equals("LowerCaseH"))
        {
            return;
        }
        if(player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals("TP-Wand"))
        {
            if(event.getRightClicked() instanceof LivingEntity)
            {
                player.sendMessage("Locked target " +ent.getName());
                plugin.tptarget = (LivingEntity) event.getRightClicked();
            }
            else {
                return;
            }

        }
    }
}
