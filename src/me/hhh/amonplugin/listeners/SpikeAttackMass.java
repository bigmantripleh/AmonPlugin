package me.hhh.amonplugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SpikeAttackMass implements Listener {

    private final JavaPlugin plugin;

    public SpikeAttackMass(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if the item is dirt with Mending level 15
        if (item.getType() == Material.DIRT && item.getEnchantmentLevel(Enchantment.MENDING) == 15) {
            // Get all targets within a 20 block radius
            List<LivingEntity> targets = getEntitiesInRange(player, 20);

            if (!targets.isEmpty()) {
                // Trigger the particle trails for each target
                for (LivingEntity target : targets) {
                    triggerParticleTrail(player, target);
                }
            }
        }
    }

    private List<LivingEntity> getEntitiesInRange(Player player, double radius) {
        Location playerLocation = player.getLocation();
        List<LivingEntity> targets = new ArrayList<>();

        for (Entity entity : player.getWorld().getNearbyEntities(playerLocation, radius, radius, radius)) {
            if (entity instanceof LivingEntity && !entity.getUniqueId().equals(player.getUniqueId())) {
                targets.add((LivingEntity) entity);
            }
        }

        return targets;
    }

    private void triggerParticleTrail(Player player, LivingEntity target) {
        // Create a particle follower
        ParticleFollower follower = new ParticleFollower(player.getLocation(), target, player);
        follower.runTaskTimer(plugin, 0L, 2L); // Every 0.1 seconds (2 ticks)
    }

    private class ParticleFollower extends BukkitRunnable {
        private Location particleLocation;
        private final LivingEntity target;
        private final Player player;
        private final double speed = 0.2; // Adjust speed here
        private int timeElapsed = 0;
        private final int maxTime = 100; // 10 seconds (100 ticks)
        private final Location startLocation;

        public ParticleFollower(Location startLocation, LivingEntity target, Player player) {
            this.particleLocation = startLocation.clone().add(0, 1.5, 0); // Start slightly above the player's feet
            this.target = target;
            this.player = player;
            this.startLocation = startLocation;
        }

        @Override
        public void run() {
            // Increment the elapsed time
            timeElapsed++;

            // If the time exceeds maxTime, cancel the task and stop the particle follower
            if (timeElapsed > maxTime) {
                this.cancel(); // Stop the particle follower
                return;
            }

            // Get the target location
            Location targetLocation = target.getLocation().clone().add(0, target.getHeight() / 2, 0);

            // Calculate the direction and distance to the target
            Vector direction = targetLocation.toVector().subtract(particleLocation.toVector()).normalize();
            double distance = particleLocation.distance(targetLocation);

            // If the distance to the target is small, treat it as the target being reached
            if (distance < 1) {
                triggerSpikeDamage(target);
                this.cancel(); // Stop the particle follower
                return;
            }

            // Move particles towards the target
            particleLocation.add(direction.multiply(speed)); // Move particle closer to target

            // Display particle effect
            particleLocation.getWorld().spawnParticle(Particle.REDSTONE, particleLocation, 1, new Particle.DustOptions(Color.RED, 1));
        }

        private void triggerSpikeDamage(LivingEntity entity) {
            Location entityLocation = entity.getLocation();
            Block blockBelow = entityLocation.clone().subtract(0, 1, 0).getBlock(); // Get the block below the entity
            Material blockTypeBelow = blockBelow.getType();

            if (blockTypeBelow == Material.AIR) {
                // If the block below is air, do not apply damage and do not spawn the spike
                return;
            }

            // Set the spike block to the type of the block below
            Block spikeBlock = entityLocation.getBlock();
            Material originalMaterial = spikeBlock.getType(); // Store the original block material
            spikeBlock.setType(blockTypeBelow);

            // Play sound effect
            entityLocation.getWorld().playSound(entityLocation, Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);

            // Apply damage and launch entity
            entity.damage(10, player); // Apply damage to the entity
            entity.setVelocity(new Vector(0, 3, 0)); // Launch upwards

            // Schedule spike removal after 1 second
            new BukkitRunnable() {
                @Override
                public void run() {
                    spikeBlock.setType(originalMaterial); // Restore the original block
                }
            }.runTaskLater(plugin, 20L); // 1 second (20 ticks)
        }
    }
}
