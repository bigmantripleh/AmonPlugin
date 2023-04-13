package me.hhh.amonplugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class DeflectTest implements Listener {

    @EventHandler
    public void deflectArrow(ProjectileHitEvent event)
    {
        if(!(event.getHitEntity() instanceof LivingEntity))
        {
            return;
        }
        LivingEntity hit = (LivingEntity) event.getHitEntity();
        if(!(hit instanceof Player)) {
           return;
        }
        Player player = (Player) hit;
        if(!(player.getName().equals("LowerCaseH")))
        {
            return;
        }
        if(!(player.getInventory().getItemInOffHand().getType() == Material.BLUE_STAINED_GLASS))
        {
            return;
        }
        Projectile projectile = event.getEntity();
        if(projectile.getType() == EntityType.ARROW)
        {
            projectile.remove();
            Location spawnlocation = player.getEyeLocation();
            spawnlocation.setY(spawnlocation.getY()+1);
            LivingEntity sender = (LivingEntity) projectile.getShooter();
            if(sender==null)
            {
                return;
            }
            Location senderlocation = sender.getLocation();
            senderlocation.setY(senderlocation.getY()+2);
            Vector vector = sender.getLocation().toVector().subtract(spawnlocation.toVector()).normalize();
            Arrow arrow = Bukkit.getWorld("world").spawnArrow(spawnlocation, vector, 2, 0);
            event.setCancelled(true);
        }
        if(projectile.getType() == EntityType.SPLASH_POTION) {
            ThrownPotion potion = (ThrownPotion) projectile;
            projectile.remove();
            Location spawnlocation = player.getEyeLocation();
            spawnlocation.setY(spawnlocation.getY()+1);
            LivingEntity sender = (LivingEntity) projectile.getShooter();
            if(sender==null)
            {
                return;
            }
            Vector vector = sender.getLocation().toVector().subtract(spawnlocation.toVector()).normalize();
            ThrownPotion returnpotion = (ThrownPotion) Bukkit.getWorld("world").spawnEntity(spawnlocation, potion.getType());
            returnpotion.setVelocity(vector);
            event.setCancelled(true);
        }
        if(projectile.getType() == EntityType.SNOWBALL)
        {

            int i = ThreadLocalRandom.current().nextInt(1, 3);
            projectile.remove();

            player.sendMessage("Oddish odd");
            if(i==1)
            {
                projectile.remove();
            }
            else{
                return;
            }

        }
    }
    
}
