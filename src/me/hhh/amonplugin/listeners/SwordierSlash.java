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
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;
import java.util.UUID;

public class SwordierSlash implements Listener {

    private final JavaPlugin plugin;
    private final Random random = new Random();

    public SwordierSlash(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Check if the action is a right-click
        if (event.getAction().toString().contains("RIGHT_CLICK")) {
            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();

            // Check if the item is a Netherite Sword with Mending 11
            if (item != null && item.getType() == Material.NETHERITE_SWORD
                    && item.getEnchantments().getOrDefault(Enchantment.MENDING, 0) == 15) {

                // Trigger the ability
                triggerSlashAbility(player);
            }
        }
    }

    private void triggerSlashAbility(Player player) {
        // Define the slash properties
        double slashWidth = 2.0;
        double slashLength = 5.0;
        double damage = 10.0;
        long disableRegenDuration = 60; // 3 seconds in ticks (20 ticks = 1 second)

        // Calculate direction
        final Vector direction = player.getLocation().getDirection().normalize();
        double rollAngle = (random.nextDouble() * 180) - 90; // Random roll between -90 and 90
        final Vector rolledDirection = rollVector(direction, rollAngle);

        // Initial warning effect
        playWarningEffect(player);

        // Create the slashes after a 0.5-second delay (10 ticks)
        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < 3; i++) {
                    new BukkitRunnable() {
                        double distanceTraveled = 0;

                        @Override
                        public void run() {
                            if (distanceTraveled > slashLength) {
                                this.cancel();
                                return;
                            }

                            // Calculate current position
                            Location start = player.getEyeLocation().add(direction.clone().multiply(distanceTraveled));

                            // Display the particles
                            spawnSlashParticles(player, start.toVector(), rolledDirection, slashWidth);

                            // Apply damage to entities
                            applySlashDamage(player, start.toVector(), rolledDirection, slashWidth, damage, disableRegenDuration);

                            distanceTraveled += 0.5; // Move the slash forward by 0.5 blocks per tick
                        }
                    }.runTaskTimer(plugin, i * 10L, 1L); // Delay each slash by 0.5 seconds (10 ticks)
                }
            }
        }.runTaskLater(plugin, 10L); // Delay for 0.5 seconds
    }

    private void playWarningEffect(Player player) {
        // Play sound and show warning particles
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.0f);

        // Create warning particle effect for 1 second
        new BukkitRunnable() {
            int iterations = 0;

            @Override
            public void run() {
                if (iterations >= 20) {
                    this.cancel();
                    return;
                }

                // Create a flash of crit magic particles
                Location eyeLocation = player.getEyeLocation();
                player.getWorld().spawnParticle(Particle.CRIT_MAGIC, eyeLocation, 10, 0.5, 0.5, 0.5, 0.1);

                iterations++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void spawnSlashParticles(Player player, Vector position, Vector direction, double width) {
        // Create an arc of particles perpendicular to the direction of the slash
        Vector perpendicular = new Vector(-direction.getZ(), 0, direction.getX()).normalize();

        for (double i = -width / 2; i <= width / 2; i += 0.5) {
            Vector offset = perpendicular.clone().multiply(i);
            player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, position.toLocation(player.getWorld()).add(offset), 0, 0, 0, 0, 1);
        }
    }

    private void applySlashDamage(Player player, Vector position, Vector direction, double width, double damage, long regenDisableDuration) {
        UUID exemptUUID = UUID.fromString("4b9596f1-6677-4f57-ad17-3a4892e5ce2d");
        for (Entity entity : player.getWorld().getNearbyEntities(position.toLocation(player.getWorld()), width, 2.0, width)) {
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;
                // Skip if the entity is a player with the exempt UUID
                if (livingEntity instanceof Player && livingEntity.getUniqueId().equals(exemptUUID)) {
                    continue;
                }
                livingEntity.damage(damage, player);

                // Cancel regeneration
                cancelRegeneration(livingEntity, regenDisableDuration);
            }
        }
    }

    private void cancelRegeneration(LivingEntity entity, long duration) {
        // Remove regeneration for the given duration
        new BukkitRunnable() {
            long ticksPassed = 0;

            @Override
            public void run() {
                if (ticksPassed >= duration) {
                    this.cancel();
                    return;
                }

                // Cancel healing and regeneration effects
                entity.removePotionEffect(PotionEffectType.REGENERATION);
                entity.removePotionEffect(PotionEffectType.HEAL);

                ticksPassed++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private Vector rollVector(Vector vector, double rollAngle) {
        // Apply roll (rotation around the direction vector)
        double radRoll = Math.toRadians(rollAngle);

        // Rotate around Z-axis (roll)
        double cosTheta = Math.cos(radRoll);
        double sinTheta = Math.sin(radRoll);

        // Calculate the rolled vector
        double newX = vector.getX() * cosTheta - vector.getY() * sinTheta;
        double newY = vector.getX() * sinTheta + vector.getY() * cosTheta;

        return new Vector(newX, newY, vector.getZ()).normalize();
    }
}
