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
import org.bukkit.util.Vector;
import org.bukkit.plugin.java.JavaPlugin;

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
                    createPillar(hitLocation);
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

    private void createPillar(Location center) {
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
            }
        }.runTaskLater(plugin, 40L); // 2 seconds later (20 ticks per second * 2 = 40 ticks)
    }


    private void applyNegativeEffects(LivingEntity entity) {
        // Apply various negative status effects
        entity.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));  // Weakness for 5 seconds
        entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1));     // Slowness for 5 seconds
        entity.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 100, 1)); // Nausea for 5 seconds
        entity.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 100, 1));    // Hunger for 5 seconds
    }
}
