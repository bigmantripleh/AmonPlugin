package me.hhh.amonplugin;

import me.hhh.amonplugin.commands.ToggleTP;
import me.hhh.amonplugin.listeners.DrawCut;
import me.hhh.amonplugin.listeners.Telettack;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin
{
  public static Plugin instance = null;
  public boolean telettack = true;

  @Override
  public void onEnable(){
    getLogger().info(ChatColor.GREEN+"AmonPlugin enabled!");
    getCommand("toggletelettack").setExecutor(new ToggleTP(this));
    getServer().getPluginManager().registerEvents(new DrawCut(), this);
    getServer().getPluginManager().registerEvents(new Telettack(this), this);
  }

  @Override
  public void onDisable(){
    getServer().getLogger().info(ChatColor.RED+"AmonPlugin disabled");
    instance = null;
  }
}
