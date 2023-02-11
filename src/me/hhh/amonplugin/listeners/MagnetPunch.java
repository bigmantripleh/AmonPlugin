package me.hhh.amonplugin.listeners;

import me.hhh.amonplugin.Main;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class MagnetPunch implements Listener {

    @EventHandler
    public void MagnetFuckery(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        Action action = event.getAction();

        if(!player.getName().equals("LowerCaseH"))
        {
            return;
        }

        if(action != Action.RIGHT_CLICK_AIR)
        {
            return;
        }

        if(event.getItem().getType() != Material.IRON_SWORD || !(event.getItem().getItemMeta().getDisplayName().equals("Magnet-Sword")))
        {
            return;
        }

        ArrayList<Entity> etargets;
        etargets = (ArrayList<Entity>) player.getNearbyEntities(30, 2, 30);
        ArrayList<LivingEntity> targets = new ArrayList<LivingEntity>();
        for(Entity entity : etargets)
        {
            if(entity instanceof LivingEntity)
            {
                if(!(entity instanceof Chicken)){
                    targets.add((LivingEntity) entity);
                }
            }
        }

        PotionEffect BLIND = new PotionEffect(PotionEffectType.BLINDNESS, 4, 10);
        PotionEffect SLOW = new PotionEffect(PotionEffectType.SLOW, 100, 10);

        for(LivingEntity target : targets)
        {
            Vector direction = player.getLocation().toVector().subtract(target.getLocation().toVector()).normalize();
            double tool = target.getLocation().distance(player.getLocation());
            direction.multiply(tool*0.7);
            target.setVelocity(direction);
        }


        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                for(LivingEntity target : targets)
                {
                    Vector direction = target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
                    direction.multiply(5);
                    target.setVelocity(direction);
                }
            }
        };
        // Run the task on this plugin in 3 seconds (60 ticks)
        task.runTaskLater(Main.instance, 20 * 1);

        Vector vector = new Vector(0, 3, 0);
        BukkitRunnable task3 = new BukkitRunnable() {
            @Override
            public void run() {
                for(LivingEntity target : targets)
                {
                    target.setVelocity(vector);
                }
            }
        };
        task3.runTaskLater(Main.instance, 20 * 2);



        World world = player.getWorld();

        BukkitRunnable task2 = new BukkitRunnable() {
            @Override
            public void run() {
                for (LivingEntity target : targets) {
                    world.strikeLightning(target.getLocation());
                }
            }
        };
        task2.runTaskLater(Main.instance, 20 * 3);

        Vector vecto2 = new Vector(0, -10, 0);
        BukkitRunnable slam = new BukkitRunnable() {
            @Override
            public void run() {
                for(LivingEntity target : targets)
                {
                    target.setVelocity(vecto2);
                }
            }
        };
        slam.runTaskLater(Main.instance, 20 * 4);

}}
