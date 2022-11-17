package me.hhh.amonplugin.commands;

import me.hhh.amonplugin.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ToggleTP implements CommandExecutor {

    private Main plugin;

    public ToggleTP(Main plugin)
    {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
      if(plugin.telettack==true)
      {
          commandSender.sendMessage("Telettack has been disabled!");
          plugin.telettack=false;
          return true;
      }
      else {
          commandSender.sendMessage("Telettack has been enabled!");
          plugin.telettack=true;
          return true;
      }
    }
}
