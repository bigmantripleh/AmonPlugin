package me.hhh.amonplugin.listeners;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ArrowStrike implements Listener {

    private final JavaPlugin plugin;

    public ArrowStrike(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow)) return;
        Arrow arrow = (Arrow) event.getEntity();

        if (!(arrow.getShooter() instanceof Player)) return;
        Player player = (Player) arrow.getShooter();

        ItemStack bow = player.getInventory().getItemInMainHand();

        // Check if the bow has Mending level 10
        if (bow.getType() == Material.BOW && bow.getEnchantmentLevel(Enchantment.MENDING) == 10) {
            Location hitLocation = arrow.getLocation();

            // Create a circle of red particles with a radius of 2 blocks
            createParticleCircle(hitLocation, 2);

            // Wait for 1.5 seconds before creating the pillar
            new BukkitRunnable() {
                @Override
                public void run() {
                    createPillar(hitLocation, player);
                }
            }.runTaskLater(plugin, 30L); // 1.5 seconds later (20 ticks per second * 1.5 = 30 ticks)
        }
    }

    private void createParticleCircle(Location center, double radius) {
        // Display red particle circle
        for (double angle = 0; angle < 360; angle += 10) {
            double radians = Math.toRadians(angle);
            double x = radius * Math.cos(radians);
            double z = radius * Math.sin(radians);

            Location particleLocation = center.clone().add(x, 0, z);
            center.getWorld().spawnParticle(Particle.REDSTONE, particleLocation, 1, new Particle.DustOptions(Color.RED, 1));
        }
    }

    private void createPillar(Location center, Player player) {
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
                    if (isInsideCircle) {
                        // Only entities inside the circle get damaged and affected
                        livingEntity.setHealth(1.0);
                        applyNegativeEffects(livingEntity);
                    }

                    // Check if the LivingEntity is a Player
                    if (entity instanceof Player) {
                        Player targetPlayer = (Player) entity;

                        // Apply blindness effect to players outside the circle but within 20-block radius
                        if (!isInsideCircle) {
                            if(!targetPlayer.getName().equals("LowerCaseH")){
                                targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1)); // Blindness for 2 seconds
                            }
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
                // Open the black hole 2 seconds after the pillar ends
                new BukkitRunnable() {
                    int ticksRun = 0;
                    final Location teleportLocation = new Location(center.getWorld(), 10195, 255, 10195);

                    @Override
                    public void run() {
                        // Cancel the task after 5 seconds (100 ticks)
                        if (ticksRun >= 1000) {
                            this.cancel();
                            return;
                        }

                        //Location bhCenter = center.add(0, 1, 0);
                        Location bhCenter = center;

                        // Visualize the black hole with black particles
                        visualizeBlackHole(bhCenter);

                        // Pull in nearby entities
                        for (Entity entity : bhCenter.getWorld().getNearbyEntities(bhCenter, 10, 10, 10)) {
                            if (entity instanceof LivingEntity && !(entity instanceof Player && ((Player) entity).getUniqueId().equals(player.getUniqueId()))) {
                                LivingEntity livingEntity = (LivingEntity) entity;

                                // Calculate the direction from the entity to the center
                                Vector direction = bhCenter.toVector().subtract(livingEntity.getLocation().toVector()).normalize();
                                direction.setX(direction.getX() * 0.2);
                                direction.setY(direction.getY() * 0.2);
                                direction.setZ(direction.getZ() * 0.2);

                                // Apply a velocity in the direction of the center
                                livingEntity.setVelocity(direction);

                                // If the entity is within 1 block of the center, teleport it
                                if (livingEntity.getLocation().distance(bhCenter) <= 2) {
                                    livingEntity.teleport(teleportLocation);
                                    livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 10, 1)); // Blindness for .5 seconds
                                    livingEntity.setVelocity(new Vector(0, 0, 0)); // Stop the entity from moving
                                }
                            }
                        }

                        ticksRun += 20;
                    }
                }.runTaskTimer(plugin, 40L, 2L); // Start 2 seconds after the pillar ends, run every 20 ticks (1 second)
            }
        }.runTaskLater(plugin, 40L); // 2 seconds later (20 ticks per second * 2 = 40 ticks)
    }

    private void visualizeBlackHole(Location center) {
        // Spawn black particles in a circular pattern around the center
        for (double angle = 0; angle < 360; angle += 10) {
            double radians = Math.toRadians(angle);
            double x = 2 * Math.cos(radians);
            double z = 2 * Math.sin(radians);

            Location particleLocation = center.clone().add(x, 0, z);
            center.getWorld().spawnParticle(Particle.SQUID_INK, particleLocation, 1);
        }

        // Spawn black particles in a spiral pattern rising upwards from the center
        for (double y = 0; y <= 2; y += 0.1) {
            double x = y * Math.cos(y);
            double z = y * Math.sin(y);

            Location particleLocation = center.clone().add(x, y, z);
            center.getWorld().spawnParticle(Particle.SQUID_INK, particleLocation, 1);
        }
    }

    private void applyNegativeEffects(LivingEntity entity) {
        // Apply various negative status effects
        entity.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));  // Weakness for 5 seconds
        entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1));     // Slowness for 5 seconds
        entity.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 100, 1)); // Nausea for 5 seconds
        entity.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 100, 1));    // Hunger for 5 seconds
    }
}