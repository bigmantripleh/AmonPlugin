package me.hhh.amonplugin.listeners;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.enchantments.Enchantment;

import java.util.UUID;

public class ProtectiveAura implements Listener {

    private final JavaPlugin plugin;

    public ProtectiveAura(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Check if the action is a right-click
        if (event.getAction().toString().contains("RIGHT_CLICK")) {
            Player player = event.getPlayer();
            if (player.getInventory().getItemInMainHand().getType() == Material.GLOWSTONE_DUST) {
                ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();
                if (meta.hasEnchant(Enchantment.MENDING) && meta.getEnchantLevel(Enchantment.MENDING) == 10) {
                    // Trigger the ability
                    triggerProtectiveAura(player);
                }
            }
        }
    }

    private void triggerProtectiveAura(Player player) {
        // Create the aura for 10 seconds (200 ticks)
        new BukkitRunnable() {
            int ticksPassed = 0;

            @Override
            public void run() {
                if (ticksPassed >= 200) {
                    this.cancel();
                    return;
                }

                // Spawn particles around the player to visualize the aura
                player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 10, 2.5, 2.5, 2.5, 0);

                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 200, 2, true, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 2, true, true));

                // Visualize the border of the aura at feet height
                for (double angle = 0; angle < 360; angle += 10) {
                    double radian = Math.toRadians(angle);
                    double x = Math.cos(radian) * 5;
                    double z = Math.sin(radian) * 5;
                    Location loc = player.getLocation().clone().add(x, 0, z);
                    player.getWorld().spawnParticle(Particle.END_ROD, loc, 1, 0, 0, 0, 0);
                }

                // Check for other players within a 5 block radius
                for (Entity entity : player.getNearbyEntities(5.0, 5.0, 5.0)) {
                    if (entity instanceof Player) {
                        Player otherPlayer = (Player) entity;

                        // Skip the player with the specified UUID
                        if (otherPlayer.getUniqueId().equals(UUID.fromString("4b9596f1-6677-4f57-ad17-3a4892e5ce2d"))) {
                            continue;
                        }



                        // Apply a damage resistance effect to the other player
                        otherPlayer.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20, 2, true, true));
                        otherPlayer.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20, 2, true, true));
                    }
                }

                ticksPassed++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}