package me.hhh.amonplugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DeathXPloss implements Listener {

  @EventHandler
    public void cutExperienceOnDeath(PlayerDeathEvent event)
  {
      if(!(event.getEntity() instanceof Player) || !(Bukkit.getServer().getOnlinePlayers().contains(event.getEntity())))
      {
          return;
      }
      Bukkit.getServer().getLogger().info(event.getEntity().getUniqueId().toString());
      if(event.getEntity().getUniqueId().toString().equals("4b9596f1-6677-4f57-ad17-3a4892e5ce2d"))
      {
          return;
      }
      if(!(event.getEntity() instanceof Player) || !(Bukkit.getServer().getOnlinePlayers().contains(event.getEntity())))
      {
          return;
      }
      Player player = event.getEntity();
      double oldXP = player.getExp();
      if(player.hasPermission("h.exp"))
      {
          double newXP = oldXP*0.9;
          player.setExp((float) newXP);
          player.sendMessage("You have died and lost "+(oldXP-newXP)+" XP");
          return;
      }
      double newXP = oldXP*0.66;
      player.setExp((float) newXP);
      player.sendMessage(ChatColor.RED+"You have died and lost "+(oldXP-newXP)+" XP");
      Bukkit.getServer().getLogger().info(player.getName()+" has died and lost "+(oldXP-newXP)+" XP");

  }

    @EventHandler
    public void killOddishKiller(EntityDeathEvent event)
    {
        if(!event.getEntity().toString().equals("CraftChicken"))
        {
            return;
        }
        Entity oddish = event.getEntity();
        Entity killer = event.getEntity().getKiller();
        if(!(killer instanceof Player))
        {
            return;
        }

        if(killer.getUniqueId().toString().equals("4b9596f1-6677-4f57-ad17-3a4892e5ce2d"))
        {
            return;
        }

        event.setDroppedExp(0);
        Player asshole = (Player) killer;

        asshole.sendMessage("Do not kill an Oddish EVER again!");

        PotionEffect poison = new PotionEffect(PotionEffectType.POISON, 2400, 3, false, false);
        PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, 2400, 3, false, false);
        PotionEffect fatigue = new PotionEffect(PotionEffectType.SLOW_DIGGING, 2400, 3, false, false);
        PotionEffect jump = new PotionEffect(PotionEffectType.JUMP, 2400, 999, false, false);
        PotionEffect blind = new PotionEffect(PotionEffectType.BLINDNESS, 2400, 3, false, false);
        PotionEffect weakness = new PotionEffect(PotionEffectType.WEAKNESS, 2400, 3, false, false);

        poison.apply(asshole);
        slow.apply(asshole);
        fatigue.apply(asshole);
        jump.apply(asshole);
        blind.apply(asshole);
        weakness.apply(asshole);

    }
}
