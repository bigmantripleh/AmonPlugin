package me.hhh.amonplugin;

import me.hhh.amonplugin.commands.SetCustomModelData;
import me.hhh.amonplugin.commands.TPSelectedEntity;
import me.hhh.amonplugin.commands.ToggleDrawCut;
import me.hhh.amonplugin.commands.ToggleTP;
import me.hhh.amonplugin.listeners.BuryTheLight;
import me.hhh.amonplugin.listeners.DrawCut;
import me.hhh.amonplugin.listeners.Telettack;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin
{
  public LivingEntity tptarget;
  public static Plugin instance = null;
  public boolean telettack = true;
  public boolean drawcut = true;

  @Override
  public void onEnable(){
    getLogger().info(ChatColor.GREEN+"AmonPlugin enabled!");
    instance = this;
    getCommand("toggletelettack").setExecutor(new ToggleTP(this));
    getCommand("toggledrawcut").setExecutor(new ToggleDrawCut(this));
    getCommand("setmodel").setExecutor(new SetCustomModelData());
    getCommand("entphere").setExecutor(new TPSelectedEntity(this));
    getServer().getPluginManager().registerEvents(new DrawCut(this), this);
    getServer().getPluginManager().registerEvents(new Telettack(this), this);
    getServer().getPluginManager().registerEvents(new BuryTheLight(this), this);
    getServer().getPluginManager().registerEvents(new TPSelectedEntity(this), this);
  }

  @Override
  public void onDisable(){
    getServer().getLogger().info(ChatColor.RED+"AmonPlugin disabled");
    instance = null;
  }
}
