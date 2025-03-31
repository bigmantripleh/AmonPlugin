package me.hhh.amonplugin;

import me.hhh.amonplugin.commands.*;
import me.hhh.amonplugin.listeners.*;
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

  public boolean bloodmoon = false;

  @Override
  public void onEnable(){
    getLogger().info(ChatColor.GREEN+"AmonPlugin enabled!");
    instance = this;
    getCommand("toggletelettack").setExecutor(new ToggleTP(this));
    getCommand("showhbar").setExecutor(new ShowBossBar(this));
    getCommand("toggledrawcut").setExecutor(new ToggleDrawCut(this));
    getCommand("setmodel").setExecutor(new SetCustomModelData());
    getCommand("entphere").setExecutor(new TPSelectedEntity(this));
    getServer().getPluginManager().registerEvents(new ArmorBreak(this), this);
    getServer().getPluginManager().registerEvents(new BlockThrow(this), this);
    getServer().getPluginManager().registerEvents(new SwordSlash(this), this);
    getServer().getPluginManager().registerEvents(new SpikeAttackMass(this), this);
    getServer().getPluginManager().registerEvents(new CatScratch(this), this);
    getServer().getPluginManager().registerEvents(new SpecialJumpAttack(this), this);
    getServer().getPluginManager().registerEvents(new BeaconBlast(this), this);
    getServer().getPluginManager().registerEvents(new CircleBlast(this), this);
    getServer().getPluginManager().registerEvents(new CatSummoner(this), this);
    getServer().getPluginManager().registerEvents(new DrawCut(this), this);
    getServer().getPluginManager().registerEvents(new Telettack(this), this);
    getServer().getPluginManager().registerEvents(new BuryTheLight(this), this);
    getServer().getPluginManager().registerEvents(new TPSelectedEntity(this), this);
    getServer().getPluginManager().registerEvents(new ProtectiveAura(this), this);
    getServer().getPluginManager().registerEvents(new DayTicket(this), this);
    getServer().getPluginManager().registerEvents(new TeleBow(this), this);
    getServer().getPluginManager().registerEvents(new ChainAttack(this), this);
    getServer().getPluginManager().registerEvents(new NightTicket(), this);
    getServer().getPluginManager().registerEvents(new BlackSun(this), this);
    getServer().getPluginManager().registerEvents(new ArrowStrike(this), this);
    getServer().getPluginManager().registerEvents(new ArrowStrikePlus(this), this);
    //getServer().getPluginManager().registerEvents(new BuryTheTeleport(this), this);
    //getServer().getPluginManager().registerEvents(new SecretBarrier(), this);
    getServer().getPluginManager().registerEvents(new BeamOfDeath(this), this);
    getServer().getPluginManager().registerEvents(new DevastatingBlow(this), this);
    getServer().getPluginManager().registerEvents(new MagnetPunch(), this);
    getServer().getPluginManager().registerEvents(new DeflectTest(), this);
    getServer().getPluginManager().registerEvents(new DeathXPloss(), this);
    getServer().getPluginManager().registerEvents(new DevastatingBlow(this), this);
    getServer().getPluginManager().registerEvents(new UltraInstinct(this), this);
    this.getServer().getPluginManager().registerEvents(new SwordierSlash(this), this);
  }

  @Override
  public void onDisable(){
    getServer().getLogger().info(ChatColor.RED+"AmonPlugin disabled");
    instance = null;
  }
}
