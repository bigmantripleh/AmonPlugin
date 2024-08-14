package me.hhh.amonplugin.listeners;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CircleBlast implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, Integer> playerRadii = new HashMap<>();
    private final Set<UUID> chargingPlayers = new HashSet<>();

    public CircleBlast(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if the item is red wool with Mending level 10
        if (item.getType() == Material.RED_WOOL && item.getEnchantmentLevel(Enchantment.MENDING) == 10) {
            UUID playerUUID = player.getUniqueId();

            // If the player is already charging, do nothing
            if (chargingPlayers.contains(playerUUID)) {
                return;
            }

            // Start the charging process
            chargingPlayers.add(playerUUID);
            playerRadii.put(playerUUID, 5);  // Start with a 5-block radius

            // Display flaming particles around the player and play sound
            player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 50, 1, 1, 1, 0.1);
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 1.0f, 1.0f);

            // Start displaying orange particles on the ground in the radius
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (chargingPlayers.contains(playerUUID)) {
                        displayOrangeParticles(player, playerRadii.get(playerUUID));
                    }
                }
            }.runTaskTimer(plugin, 0L, 10L); // Run every 10 ticks (0.5 seconds)

            // Schedule the attack after 5 seconds
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (chargingPlayers.contains(playerUUID)) {
                        launchAttack(player);
                        chargingPlayers.remove(playerUUID);
                    }
                }
            }.runTaskLater(plugin, 100L); // 100 ticks = 5 seconds
        }
    }

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            UUID playerUUID = player.getUniqueId();

            // Check if the player is in the charging process
            if (chargingPlayers.contains(playerUUID)) {
                // Increase the area radius by 1 block
                int newRadius = playerRadii.get(playerUUID) + 1;
                playerRadii.put(playerUUID, newRadius);

                // Reduce the incoming damage by half
                event.setDamage(event.getDamage() / 2);
            }
        }
    }

    private void displayOrangeParticles(Player player, int radius) {
        Location center = player.getLocation();
        World world = player.getWorld();
        double y = center.getY();

        // Display orange particles on the ground within the radius
        for (double angle = 0; angle < 360; angle += 5) {
            double radians = Math.toRadians(angle);
            double x = radius * Math.cos(radians);
            double z = radius * Math.sin(radians);

            Location particleLocation = new Location(world, center.getX() + x, y, center.getZ() + z);
            world.spawnParticle(Particle.REDSTONE, particleLocation, 1, new Particle.DustOptions(Color.ORANGE, 1.5f));
        }
    }

    private void launchAttack(Player player) {
        UUID playerUUID = player.getUniqueId();
        int radius = playerRadii.getOrDefault(playerUUID, 5); // Get the current radius

        // Get all entities in the radius
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;

                // Exclude chickens and the player themselves
                if (livingEntity.getType() != EntityType.CHICKEN && !livingEntity.getUniqueId().equals(playerUUID)) {
                    // Apply knockback
                    Vector knockback = livingEntity.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(3);
                    livingEntity.setVelocity(knockback);

                    // Apply nausea effect
                    livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 140, 1)); // 140 ticks = 7 seconds
                }
            }
        }

        // Play final explosion sound and particle effect
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
        player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, player.getLocation(), 10, radius, 1, radius, 0.1);

        // Clean up the player's data
        playerRadii.remove(playerUUID);
    }
}
