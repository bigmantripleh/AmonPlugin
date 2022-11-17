package me.hhh.amonplugin.commands;

import me.hhh.amonplugin.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ToggleDrawCut implements CommandExecutor {

    private Main plugin;

    public ToggleDrawCut(Main plugin)
    {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(plugin.drawcut==true)
        {
            commandSender.sendMessage("DrawCut has been disabled!");
            plugin.drawcut=false;
            return true;
        }
        else {
            commandSender.sendMessage("DrawCut has been enabled!");
            plugin.drawcut=true;
            return true;
        }
    }
}