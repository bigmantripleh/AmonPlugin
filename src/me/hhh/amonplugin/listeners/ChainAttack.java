package me.hhh.amonplugin.listeners;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChainAttack implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public ChainAttack(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Check if the action is a right-click
        if (event.getAction().toString().contains("RIGHT_CLICK")) {
            Player player = event.getPlayer();
            if (player.getInventory().getItemInMainHand().getType() == Material.IRON_INGOT) {
                ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();
                if (meta.hasEnchant(Enchantment.MENDING) && meta.getEnchantLevel(Enchantment.MENDING) == 10) {
                    // Check for cooldown
                    if (cooldowns.containsKey(player.getUniqueId()) && cooldowns.get(player.getUniqueId()) > System.currentTimeMillis()) {
                        player.sendMessage(ChatColor.RED + "Chain attack is on cooldown!");
                        return;
                    }

                    // Trigger the ability
                    triggerChainAttack(player);

                    // Set cooldown
                    cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + 7000); // 7 seconds cooldown
                }
            }
        }
    }


    private void triggerChainAttack(Player player) {
        LivingEntity target = getClosestTarget(player, 7.0);
        if (target != null) {
            chainAttack(player, target, 8);
        }
    }

    private LivingEntity getClosestTarget(LivingEntity player, double maxDistance) {
        Location playerLocation = player.getLocation();
        Vector direction = playerLocation.getDirection().normalize();

        double closestDistance = maxDistance;
        LivingEntity closestTarget = null;

        for (Entity entity : player.getWorld().getNearbyEntities(playerLocation, maxDistance, maxDistance, maxDistance)) {
            if (entity instanceof LivingEntity && !entity.getUniqueId().equals(player.getUniqueId())) {
                LivingEntity livingEntity = (LivingEntity) entity;
                Location entityLocation = livingEntity.getLocation();
                Vector toEntity = entityLocation.toVector().subtract(playerLocation.toVector()).normalize();
                //double angle = direction.angle(toEntity);

                if (playerLocation.distance(entityLocation) < closestDistance) { //angle < Math.toRadians(30)
                    closestDistance = playerLocation.distance(entityLocation);
                    closestTarget = livingEntity;
                }
            }
        }
        return closestTarget;
    }

    private void chainAttack(LivingEntity player, LivingEntity target, double damage) {
        // Create a particle line from the player to the target
        createParticleLine(player.getLocation(), target.getLocation(), Particle.DAMAGE_INDICATOR);

        // Damage the target
        target.damage(damage);

        // Find the next target
        LivingEntity nextTarget = getClosestTarget(target, 4.0);
        if (nextTarget != null) {

            chainAttack(target, nextTarget, damage > 1 ? damage / 1.3 : 1);


        }
    }

    private void createParticleLine(Location from, Location to, Particle particle) {
        Vector direction = to.toVector().subtract(from.toVector()).normalize();
        double distance = from.distance(to);

        for (double d = 0; d <= distance; d += 0.5) {
            Location location = from.clone().add(direction.clone().multiply(d));
            from.getWorld().spawnParticle(particle, location, 1);
        }
    }
}