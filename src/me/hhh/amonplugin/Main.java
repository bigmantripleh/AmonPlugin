package me.hhh.amonplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin
{
  @Override
  public void onEnable(){
    Bukkit.getServer().getLogger().info(ChatColor.GREEN+"AmonPlugin enabled!");
  }

  @Override
  public void onDisable(){
    Bukkit.getServer().getLogger().info(ChatColor.RED+"AmonPlugin disabled");
  }
}
