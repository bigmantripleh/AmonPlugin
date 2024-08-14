package me.hhh.amonplugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class BeaconBlast implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public BeaconBlast(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if the item is a beacon with Mending level 10
        if (item.getType() == Material.BEACON && item.getEnchantmentLevel(Enchantment.MENDING) == 10) {
            UUID playerUUID = player.getUniqueId();

            // Check if the player is on cooldown
            if (isOnCooldown(playerUUID)) {
                player.sendMessage("§cYou must wait for the cooldown to end before using this ability again.");
                return;
            }

            // Set the cooldown for the player
            setCooldown(playerUUID, 15);

            Location playerLocation = player.getLocation();

            // Calculate the location 2 blocks above the player's head
            Location beaconLocation = playerLocation.clone().add(0, 2, 0);

            // Spawn a beacon block at the calculated location
            player.getWorld().getBlockAt(beaconLocation).setType(Material.BEACON);

            // Start the laser attack
            startLaserAttack(player, beaconLocation);
        }
    }

    private void startLaserAttack(final Player player, final Location beaconLocation) {
        // Use an AtomicBoolean to manage the running state
        final AtomicBoolean isRunning = new AtomicBoolean(true);
        // Define a wrapper class to hold counter value
        class CounterWrapper {
            int value = 100;
        }
        final CounterWrapper counterWrapper = new CounterWrapper();

        // Store the runnable instance
        final BukkitRunnable laserRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (counterWrapper.value <= 0 || !isRunning.get()) {
                    // Stop firing small lasers
                    this.cancel();
                    return;
                }

                // Adjust particle color based on counter
                Particle.DustOptions particleColor;
                if (counterWrapper.value <= 70 && counterWrapper.value > 40) {
                    particleColor = new Particle.DustOptions(Color.YELLOW, 1.5f);
                } else if (counterWrapper.value <= 40) {
                    particleColor = new Particle.DustOptions(Color.GREEN, 1.5f);
                } else {
                    particleColor = new Particle.DustOptions(Color.RED, 1.5f);
                }

                // Create a cloud of particles around the beacon
                createParticleCloud(beaconLocation, particleColor);

                // Get nearby entities within a 5 block radius
                List<Entity> nearbyEntities = new ArrayList<>(beaconLocation.getWorld().getNearbyEntities(beaconLocation, 7, 7, 7));

                for (Entity entity : nearbyEntities) {
                    if (entity instanceof LivingEntity && !entity.getUniqueId().equals(player.getUniqueId())) {
                        LivingEntity livingEntity = (LivingEntity) entity;
                        shootLaser(beaconLocation, livingEntity);
                        livingEntity.damage(1, player); // Deal 1 HP damage
                        counterWrapper.value--; // Decrease counter for each hit
                    }
                }
            }
        };

        laserRunnable.runTaskTimer(plugin, 0L, 10L); // Schedule task every 0.5 seconds (10 ticks)

        // Schedule the final laser attack after 10 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                // Cancel the laser shooting runnable
                isRunning.set(false);
                laserRunnable.cancel();

                // Execute the final laser attack
                executeFinalLaser(player, beaconLocation, counterWrapper.value);

                // Remove the beacon block
                beaconLocation.getWorld().getBlockAt(beaconLocation).setType(Material.AIR);

                // Clear particles by creating a 'removal' effect (This won't directly clear, but helps reduce lingering effects)
                for (double angle = 0; angle < 360; angle += 10) {
                    double radians = Math.toRadians(angle);
                    double x = 1.5 * Math.cos(radians);
                    double z = 1.5 * Math.sin(radians);
                    Location particleLocation = beaconLocation.clone().add(x, 0, z);
                    beaconLocation.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, particleLocation, 0);
                }
            }
        }.runTaskLater(plugin, 200L); // Run this task after 200 ticks (10 seconds)
    }

    private void createParticleCloud(Location beaconLocation, Particle.DustOptions particleColor) {
        double radius = 1.5; // Radius of the particle cloud
        for (double angle = 0; angle < 360; angle += 10) {
            double radians = Math.toRadians(angle);
            double x = radius * Math.cos(radians);
            double z = radius * Math.sin(radians);

            Location particleLocation = beaconLocation.clone().add(x, 0, z);
            beaconLocation.getWorld().spawnParticle(Particle.REDSTONE, particleLocation, 5, particleColor);
        }
    }

    private void shootLaser(Location beaconLocation, LivingEntity target) {
        // Play laser sound effect
        beaconLocation.getWorld().playSound(beaconLocation, Sound.ENTITY_ENDER_DRAGON_SHOOT, 1.0f, 1.5f);

        // Create a redstone particle line between the beacon and the target
        Location targetLocation = target.getLocation().add(0, target.getHeight() / 2, 0);
        Vector direction = targetLocation.toVector().subtract(beaconLocation.toVector()).normalize();

        double distance = beaconLocation.distance(targetLocation);
        for (double i = 0; i < distance; i += 0.5) {
            Vector currentPosition = beaconLocation.toVector().add(direction.clone().multiply(i));
            Location currentLocation = new Location(beaconLocation.getWorld(), currentPosition.getX(), currentPosition.getY(), currentPosition.getZ());
            beaconLocation.getWorld().spawnParticle(Particle.REDSTONE, currentLocation, 5, new Particle.DustOptions(Color.RED, 1.0f));
        }
    }

    private void executeFinalLaser(Player player, Location beaconLocation, int counter) {
        // Get nearby entities within a 20 block radius
        List<LivingEntity> nearbyEntities = new ArrayList<>();
        for (Entity entity : beaconLocation.getWorld().getNearbyEntities(beaconLocation, 30, 30, 30)) {
            if (entity instanceof LivingEntity && !entity.getUniqueId().equals(player.getUniqueId())) {
                nearbyEntities.add((LivingEntity) entity);
            }
        }

        // Find the entity with the highest HP, excluding the player
        LivingEntity highestHpEntity = null;
        double highestHp = 0;
        for (LivingEntity entity : nearbyEntities) {
            if (entity.getHealth() > highestHp) {
                highestHpEntity = entity;
                highestHp = entity.getHealth();
            }
        }

        // If no entities are found in the range, just return
        if (highestHpEntity == null) {
            player.sendMessage("§cNo valid targets found for the final laser attack.");
            return;
        }

        // Find the next highest HP entity
        LivingEntity nextHighestHpEntity = null;
        double nextHighestHp = 0;
        for (LivingEntity entity : nearbyEntities) {
            if (entity != highestHpEntity && entity.getHealth() > nextHighestHp) {
                nextHighestHpEntity = entity;
                nextHighestHp = entity.getHealth();
            }
        }

        // If the player has the highest HP and no other valid targets, target the next highest HP entity
        if (highestHpEntity.getUniqueId().equals(player.getUniqueId()) && nextHighestHpEntity != null) {
            highestHpEntity = nextHighestHpEntity;
        }

        // Calculate damage based on the counter
        double maxHealth = highestHpEntity.getMaxHealth();
        double damage;
        if (counter > 70) {
            damage = maxHealth * 0.8; // 80% of max HP
        } else if (counter > 40) {
            damage = maxHealth * 0.5; // 50% of max HP
        } else {
            damage = maxHealth * 0.2; // 20% of max HP
        }

        // Play final laser sound effect
        beaconLocation.getWorld().playSound(beaconLocation, Sound.ENTITY_WITHER_DEATH, 1.0f, 1.0f);

        // Create a final laser effect
        createFinalLaserEffect(beaconLocation, highestHpEntity.getLocation());

        // Apply damage to the entity
        highestHpEntity.damage(damage, player);
    }

    private void createFinalLaserEffect(Location startLocation, Location endLocation) {
        Vector direction = endLocation.toVector().subtract(startLocation.toVector()).normalize();

        double distance = startLocation.distance(endLocation);
        for (double i = 0; i < distance; i += 0.25) { // Halve the increment to double the duration
            Vector currentPosition = startLocation.toVector().add(direction.clone().multiply(i));
            Location currentLocation = new Location(startLocation.getWorld(), currentPosition.getX(), currentPosition.getY(), currentPosition.getZ());

            // Create red circles around the laser line
            for (double angle = 0; angle < 360; angle += 10) {
                double radians = Math.toRadians(angle);
                double x = 1.5 * Math.cos(radians);
                double z = 1.5 * Math.sin(radians);
                Location circleLocation = currentLocation.clone().add(x, 0, z);
                startLocation.getWorld().spawnParticle(Particle.REDSTONE, circleLocation, 1, new Particle.DustOptions(Color.RED, 1.0f));
            }

            startLocation.getWorld().spawnParticle(Particle.END_ROD, currentLocation, 10);
            startLocation.getWorld().spawnParticle(Particle.FLASH, currentLocation, 5);
        }

        // Create lingering smoke effect at the end location
        new BukkitRunnable() {
            int smokeCounter = 0; // Keep track of time for smoke

            @Override
            public void run() {
                if (smokeCounter >= 60) { // 3 seconds of smoke (20 ticks per second)
                    this.cancel();
                    return;
                }

                endLocation.getWorld().spawnParticle(Particle.SMOKE_LARGE, endLocation, 30, 0.5, 0.5, 0.5, 0.02);
                smokeCounter++;
            }
        }.runTaskTimer(plugin, 0L, 1L); // Smoke appears every tick (50ms)
    }

    private boolean isOnCooldown(UUID playerUUID) {
        if (!cooldowns.containsKey(playerUUID)) {
            return false;
        }

        long timeLeft = cooldowns.get(playerUUID) - System.currentTimeMillis();
        return timeLeft > 0;
    }

    private void setCooldown(UUID playerUUID, int seconds) {
        long cooldownTime = System.currentTimeMillis() + (seconds * 1000L);
        cooldowns.put(playerUUID, cooldownTime);

        // Schedule a task to remove the player from cooldowns after the cooldown period
        new BukkitRunnable() {
            @Override
            public void run() {
                cooldowns.remove(playerUUID);
            }
        }.runTaskLater(plugin, seconds * 20L); // Convert seconds to ticks
    }
}
