
package me.hhh.amonplugin.listeners;
/*
import me.hhh.amonplugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class SecretBarrier implements Listener {

    boolean damage=true;

    @EventHandler
    public void BarrierAttack(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();

        if (!(player.getName().equals("LowerCaseH"))) {
            player.sendMessage("Not H");
            return;
        }

        if ((event.getItem().getType() != Material.RED_WOOL)) {
            player.sendMessage("Not wool!");
            return;
        }

        if (!(event.getItem().getItemMeta().getDisplayName().equals("BarrierAttack"))) {
            player.sendMessage("Wrong name!");
            return;
        }

        player.sendMessage("You did it!");
        int juergen = 3;

        Location base = player.getLocation();

        ArrayList<Location> barrierblocks = new ArrayList<Location>();
        ArrayList<Block> blockmaterials = new ArrayList<Block>();
        List<Entity> targets = player.getNearbyEntities(2, 2, 2);
        ArrayList<LivingEntity> realTargets = new ArrayList<LivingEntity>();
        for (Entity entity : targets) {
            if ((entity.getClass().getName().equals("LivingEntity"))) {
                targets.remove(entity);
            }
        }
    }

    @EventHandler
    public void Damage(EntityDamageByEntityEvent event) {
        if(!damage)
        {
            return;
        }

        if(!(event.getDamager().getName().equals("LowerCaseH")))
        {
            return;
        }

        if(!(event.getEntity() instanceof LivingEntity))
        {
            return;
        }
        LivingEntity target = (LivingEntity) event.getEntity();
        int noDamage = target.getNoDamageTicks();
        target.setNoDamageTicks(0);


        new BukkitRunnable() {
            int h = 0;

            public void run() {
                h++;
                Bukkit.getServer().broadcastMessage("odd");
                Player player = (Player) event.getDamager();
                player.setHealth(player.getHealth()-1);
                if(target.getHealth()>0)
                {
                    target.setHealth(target.getHealth()-1);
                }
                if(h>=10)
                {
                    this.cancel();
                }
            }
        }.runTaskTimer(Main.instance, 0, 3);
        target.setNoDamageTicks(noDamage);

        Vector direction = target.getLocation().toVector().subtract(event.getDamager().getLocation().toVector()).normalize();
        direction = direction.multiply(3);


        target.setVelocity(direction);
    }
}
*/