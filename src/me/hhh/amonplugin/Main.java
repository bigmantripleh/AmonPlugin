package me.hhh.amonplugin;

import me.hhh.amonplugin.commands.*;
import me.hhh.amonplugin.listeners.*;
import me.hhh.amonplugin.listeners.EventAbilities.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    public LivingEntity tptarget;
    public static Plugin instance = null;
    public boolean telettack = true;
    public boolean drawcut = true;

    public boolean bloodmoon = false;

    @Override
    public void onEnable() {
        getLogger().info(ChatColor.GREEN + "AmonPlugin enabled!");
        instance = this;
        getCommand("toggletelettack").setExecutor(new ToggleTP(this));
        getCommand("toggledrawcut").setExecutor(new ToggleDrawCut(this));
        getCommand("setmodel").setExecutor(new SetCustomModelData());
        getCommand("showhbar").setExecutor(new ShowBossBar(this));
        getCommand("entphere").setExecutor(new TPSelectedEntity(this));
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new HealthBarListener(this), this);
        pm.registerEvents(new ArmorBreak(this), this);
        pm.registerEvents(new BlockThrow(this), this);
        pm.registerEvents(new SwordSlash(this), this);
        pm.registerEvents(new SwordierSlash(this), this);
        pm.registerEvents(new SpikeAttackMass(this), this);
        pm.registerEvents(new CatScratch(this), this);
        pm.registerEvents(new SpecialJumpAttack(this), this);
        pm.registerEvents(new BeaconBlast(this), this);
        pm.registerEvents(new CircleBlast(this), this);
        pm.registerEvents(new CatSummoner(this), this);
        pm.registerEvents(new DrawCut(this), this);
        pm.registerEvents(new Telettack(this), this);
        pm.registerEvents(new BuryTheLight(this), this);
        pm.registerEvents(new TPSelectedEntity(this), this);
        pm.registerEvents(new ProtectiveAura(this), this);
        pm.registerEvents(new DayTicket(this), this);
        pm.registerEvents(new TeleBow(this), this);
        pm.registerEvents(new ChainAttack(this), this);
        pm.registerEvents(new NightTicket(), this);
        pm.registerEvents(new FoxCourier(this), this);
        pm.registerEvents(new MagnetPunch(), this);
        pm.registerEvents(new DeflectTest(), this);
        pm.registerEvents(new BeamOfDeath(this), this);
        pm.registerEvents(new BetterBlockThrow(this), this);
        pm.registerEvents(new ConeAttack(this), this);
        pm.registerEvents(new MathStrike(this), this);
        pm.registerEvents(new Kamehameha(this), this);
        pm.registerEvents(new SunfireCape(this), this);
        pm.registerEvents(new Heartsteal(this), this);
        pm.registerEvents(new CursedAxe(this), this);
        pm.registerEvents(new ExplosiveSpear(this), this);
        pm.registerEvents(new SpearOfLonginus(this), this);
        pm.registerEvents(new BlackSun(this), this);
        pm.registerEvents(new ArrowStrike(this), this);
        pm.registerEvents(new ArrowStrikePlus(this), this);
        pm.registerEvents(new DeathXPloss(), this);
        pm.registerEvents(new DevastatingBlow(this), this);
        pm.registerEvents(new UltraInstinct(this), this);
    }

    @Override
    public void onDisable() {
        getServer().getLogger().info(ChatColor.RED + "AmonPlugin disabled");
        instance = null;
    }
}
