package me.hhh.amonplugin.listeners;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CircleBlast implements Listener {

    private JavaPlugin plugin;

    // Store the activation state of players
    private final Map<UUID, Integer> playerRadius = new HashMap<>();

    public CircleBlast(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if the item is a red wool block with Mending level 10
        if (item.getType() == Material.RED_WOOL && item.getEnchantmentLevel(Enchantment.MENDING) == 10) {
            // If the player is already activating, extend the radius
            if (playerRadius.containsKey(player.getUniqueId())) {
                extendRadius(player);
            } else {
                // Activate the circle blast with the initial radius
                activateCircleBlast(player, 5);
            }
        }
    }

    private void activateCircleBlast(Player player, int initialRadius) {
        // Store player's initial activation radius
        playerRadius.put(player.getUniqueId(), initialRadius);
        Location playerLocation = player.getLocation();

        // Start a 5-second countdown
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks % 20 == 0) { // Play ticking sound every second
                    player.getWorld().playSound(playerLocation, Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                }

                if (ticks >= 100) { // 5 seconds (20 ticks per second)
                    // Apply damage and knockback after 5 seconds
                    applyCircleBlast(player, playerLocation);
                    cancel();
                    return;
                }

                // Display particles with current radius
                int currentRadius = playerRadius.get(player.getUniqueId());
                displayParticleCircle(playerLocation, currentRadius);

                // Display the particle H at the center
                displayParticleH(playerLocation);

                ticks += 5;
            }
        }.runTaskTimer(plugin, 0L, 5L); // Schedule task every 5 ticks (0.25 seconds)
    }

    private void extendRadius(Player player) {
        // Increase the radius to 10
        playerRadius.put(player.getUniqueId(), 10);
        player.sendMessage("The circle blast radius has been extended to 10 blocks!");
    }

    private void displayParticleCircle(Location center, double radius) {
        // Display particles in a circle
        for (double angle = 0; angle < 360; angle += 10) {
            double radians = Math.toRadians(angle);
            double x = radius * Math.cos(radians);
            double z = radius * Math.sin(radians);

            Location particleLocation = center.clone().add(x, 1, z);
            center.getWorld().spawnParticle(Particle.REDSTONE, particleLocation, 1, new Particle.DustOptions(org.bukkit.Color.RED, 1));
        }
    }

    private void applyCircleBlast(Player player, Location center) {
        // Get the final radius
        int finalRadius = playerRadius.get(player.getUniqueId());

        // Play explosion sound effect at the center
        center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);

        // Create rising particles effect
        new BukkitRunnable() {
            double yOffset = 0;

            @Override
            public void run() {
                if (yOffset > 2) { // Stop after reaching 2 blocks high
                    cancel();
                    return;
                }

                // Create the ring of particles
                for (double angle = 0; angle < 360; angle += 10) {
                    double radians = Math.toRadians(angle);
                    double x = finalRadius * Math.cos(radians);
                    double z = finalRadius * Math.sin(radians);

                    Location particleLocation = center.clone().add(x, yOffset, z);
                    center.getWorld().spawnParticle(Particle.REDSTONE, particleLocation, 1, new Particle.DustOptions(org.bukkit.Color.WHITE, 1));
                }

                yOffset += 0.1; // Increment the height
            }
        }.runTaskTimer(plugin, 0L, 2L); // Schedule task every 2 ticks (0.1 seconds)

        // Damage and knockback entities within the final radius
        for (Entity entity : center.getWorld().getNearbyEntities(center, finalRadius, finalRadius, finalRadius)) {
            if (entity instanceof LivingEntity && entity.getType() != EntityType.PLAYER && entity.getType() != EntityType.CHICKEN) {
                LivingEntity livingEntity = (LivingEntity) entity;

                // Apply damage
                livingEntity.damage(5, player);

                // Apply increased knockback
                Vector knockback = livingEntity.getLocation().toVector().subtract(center.toVector()).normalize().multiply(3.0); // Double knockback
                livingEntity.setVelocity(knockback);
            }
        }

        // Remove player's state after explosion
        playerRadius.remove(player.getUniqueId());
    }

    private void displayParticleH(Location center) {
        // Define the points for the H shape
        double[][] hCoordinates = {
                // Left vertical line
                {-2, 0}, {-2, 1}, {-2, 2}, {-2, -1}, {-2, -2},
                // Right vertical line
                {2, 0}, {2, 1}, {2, 2}, {2, -1}, {2, -2},
                // Thinner middle horizontal line
                {-1, 0}, {0, 0}, {1, 0}
        };

        // Particle settings
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.YELLOW, 1.5f);

        // Create H shape
        for (double[] coordinate : hCoordinates) {
            double xOffset = coordinate[0];
            double zOffset = coordinate[1];
            Location particleLocation = center.clone().add(xOffset, 1, zOffset);
            center.getWorld().spawnParticle(Particle.REDSTONE, particleLocation, 1, dustOptions);
        }
    }

}
