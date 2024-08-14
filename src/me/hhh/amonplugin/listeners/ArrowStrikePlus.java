package me.hhh.amonplugin.listeners;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;
import java.util.UUID;

public class ArrowStrikePlus implements Listener {

    private final JavaPlugin plugin;
    private final Random random = new Random();

    public ArrowStrikePlus(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow)) return;
        Arrow arrow = (Arrow) event.getEntity();

        if (!(arrow.getShooter() instanceof Player)) return;
        Player player = (Player) arrow.getShooter();

        ItemStack bow = player.getInventory().getItemInMainHand();

        // Check if the bow has Mending level 15
        if (bow.getType() == Material.BOW && bow.getEnchantmentLevel(Enchantment.MENDING) == 15) {
            Location hitLocation = arrow.getLocation();

            UUID shooterId = player.getUniqueId(); // Store the shooter's UUID

            // Create the first pillar with the particle circle
            createParticleCircle(hitLocation, 2, () -> {
                createPillar(hitLocation, shooterId, () -> {
                    // Create two additional pillars after the first pillar disappears
                    createAdditionalPillars(hitLocation, 2, shooterId, () -> {
                        // Create three more random pillars after the second set disappears
                        for (int i = 0; i < 3; i++) {
                            Location randomLocation = hitLocation.clone().add(
                                    (random.nextDouble() - 0.5) * 10,
                                    0,
                                    (random.nextDouble() - 0.5) * 10
                            );
                            createParticleCircle(randomLocation, 2, () -> {
                                createPillar(randomLocation, shooterId, null); // Final set of pillars without further pillars
                            });
                        }
                    });
                });
            });
        }
    }

    private void createPillar(Location center, UUID shooterId, Runnable afterPillars) {
        // Display the vertical pillar with increased height
        for (double y = 0; y <= 20; y += 0.5) { // Create pillar up to 20 blocks high
            Location particleLocation = center.clone().add(0, y, 0);
            center.getWorld().spawnParticle(Particle.END_ROD, particleLocation, 10, 0.3, 0.3, 0.3, 0.05); // Bright particles for the pillar
        }

        // Play a sound effect at the location of the pillar
        center.getWorld().playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);

        // Loop through nearby entities to apply effects
        for (Entity entity : center.getWorld().getNearbyEntities(center, 20, 20, 20)) { // 20-block radius
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;

                // Check if the entity is within the particle circle (2-block radius)
                double distanceToCenter = center.distance(livingEntity.getLocation());
                boolean isInsideCircle = distanceToCenter <= 2;

                if (livingEntity.getType() != EntityType.CHICKEN) {
                    // Skip the shooter
                    if (livingEntity instanceof Player) {
                        Player targetPlayer = (Player) livingEntity;
                        if (targetPlayer.getUniqueId().equals(shooterId)) {
                            continue; // Skip shooter
                        }
                    }

                    if (isInsideCircle) {
                        // Only entities inside the circle get damaged and affected
                        livingEntity.setHealth(1.0);
                        applyNegativeEffects(livingEntity);
                    }

                    // Apply blindness effect to players outside the circle but within 20-block radius
                    if (livingEntity instanceof Player) {
                        Player targetPlayer = (Player) livingEntity;
                        if (!isInsideCircle && !targetPlayer.getName().equals("LowerCaseH")) {
                            targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1)); // Blindness for 2 seconds
                        }
                    }
                }
            }
        }

        // Remove the pillar after 2 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                // Remove particles by simply not spawning new ones
                if (afterPillars != null) {
                    afterPillars.run();
                }
            }
        }.runTaskLater(plugin, 40L); // 2 seconds later (20 ticks per second * 2 = 40 ticks)
    }

    private void createAdditionalPillars(Location center, int numPillars, UUID shooterId, Runnable afterPillars) {
        // Create additional pillars with angle-based placement to avoid overlap
        double angleBetweenPillars = 360.0 / numPillars; // Calculate angle to space out pillars evenly
        double radius = 5; // Set a fixed radius for pillar placement

        for (int i = 0; i < numPillars; i++) {
            double angle = i * angleBetweenPillars;
            double radians = Math.toRadians(angle);
            double xOffset = radius * Math.cos(radians);
            double zOffset = radius * Math.sin(radians);

            Location offsetLocation = center.clone().add(xOffset, 0, zOffset);
            createParticleCircle(offsetLocation, 2, () -> {
                createPillar(offsetLocation, shooterId, null); // No additional pillars after this
            });
        }

        // Remove additional pillars after 2 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                // Proceed to the next phase
                if (afterPillars != null) {
                    afterPillars.run();
                }
            }
        }.runTaskLater(plugin, 40L); // 2 seconds later (20 ticks per second * 2 = 40 ticks)
    }

    private void createParticleCircle(Location center, double radius, Runnable afterParticles) {
        // Display red particle circle
        for (double angle = 0; angle < 360; angle += 10) {
            double radians = Math.toRadians(angle);
            double x = radius * Math.cos(radians);
            double z = radius * Math.sin(radians);

            Location particleLocation = center.clone().add(x, 0, z);
            center.getWorld().spawnParticle(Particle.REDSTONE, particleLocation, 1, new Particle.DustOptions(Color.RED, 1));
        }

        // Remove particle circle after 1 second
        new BukkitRunnable() {
            @Override
            public void run() {
                if (afterParticles != null) {
                    afterParticles.run();
                }
            }
        }.runTaskLater(plugin, 20L); // 1 second later (20 ticks per second * 1 = 20 ticks)
    }

    private void applyNegativeEffects(LivingEntity entity) {
        // Apply various negative status effects
        entity.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));  // Weakness for 5 seconds
        entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1));     // Slowness for 5 seconds
        entity.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 100, 1)); // Nausea for 5 seconds
        entity.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 100, 1));    // Hunger for 5 seconds
    }
}
