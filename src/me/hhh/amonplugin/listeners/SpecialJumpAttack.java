package me.hhh.amonplugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class SpecialJumpAttack implements Listener {
    private final JavaPlugin plugin;

    public SpecialJumpAttack(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Check if the player is holding a Mending 10 Feather
        if (player.getInventory().getItemInMainHand().getType() == Material.FEATHER &&
                player.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.MENDING) == 10) {

            if (event.getAction().toString().contains("RIGHT_CLICK")) {
                executeSpecialJumpAttack(player);
            }
        }
    }

    private void executeSpecialJumpAttack(Player player) {
        // Launch the player into the air
        Vector leap = player.getLocation().getDirection().multiply(1.5).setY(1);
        player.setVelocity(leap);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.0f);

        // Delay the slam by 1 second (20 ticks)
        new BukkitRunnable() {
            @Override
            public void run() {
                Vector direction = player.getLocation().getDirection();
                player.setVelocity(direction.multiply(0.5).setY(-2));

                // Slam damage and shockwave after landing
                Bukkit.getScheduler().runTaskLater(plugin, () -> createShockwave(player), 10);
            }
        }.runTaskLater(plugin, 20);
    }

    private void createShockwave(Player player) {
        // Get player location for impact
        Vector impactPoint = player.getLocation().toVector();
        List<Entity> nearbyEntities = player.getNearbyEntities(2, 2, 2);

        // Damage entities in a 2-block radius by 1/3 of their max HP
        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity && entity != player) {
                LivingEntity livingEntity = (LivingEntity) entity;
                double maxHealth = livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                livingEntity.damage(maxHealth / 3, player);
            }
        }

        // Play the particle effect and create shockwave
        player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, player.getLocation(), 1);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);

        new BukkitRunnable() {
            private int radius = 1;

            @Override
            public void run() {
                if (radius > 10) {
                    cancel();
                    return;
                }

                // Create the shockwave effect
                for (double angle = 0; angle < 360; angle += 20) {
                    double radians = Math.toRadians(angle);
                    double x = impactPoint.getX() + radius * Math.cos(radians);
                    double z = impactPoint.getZ() + radius * Math.sin(radians);
                    player.getWorld().spawnParticle(Particle.SMOKE_LARGE, x, player.getLocation().getY(), z, 1);

                    List<Entity> entities = (List<Entity>) player.getWorld().getNearbyEntities(impactPoint.toLocation(player.getWorld()), radius, radius, radius);
                    for (Entity entity : entities) {
                        if (entity instanceof LivingEntity && entity != player) {
                            entity.setVelocity(new Vector(0, 0.5, 0));
                            ((LivingEntity) entity).damage(5.0, player);
                        }
                    }
                }
                radius++;
            }
        }.runTaskTimer(plugin, 0, 2);
    }
}