package me.hhh.amonplugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class UltraInstinct implements Listener {

    // Set to keep track of players with the active ability
    private final Set<UUID> activeAbilityPlayers = new HashSet<>();

    final JavaPlugin plugin;

    public UltraInstinct(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Check if the action is right-click on a block
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (event.getAction().toString().contains("RIGHT_CLICK")) {
            // Check if the clicked block is obsidian
            if (item.getType()==Material.NETHER_STAR && item.getEnchantmentLevel(Enchantment.MENDING) == 10) {
                    // Trigger the ability
                    activateAbility(player);
            }
        }
    }

    @EventHandler
    public void onEntityDamaged(EntityDamageEvent event) {
        // Check if the entity is a player
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            // Check if the player has the active ability
            if (activeAbilityPlayers.contains(player.getUniqueId())) {
                // Cancel the damage
                event.setCancelled(true);

                // Perform the teleportation effect
                performTeleportation(player);
            }
        }
    }

    private void activateAbility(Player player) {
        UUID playerId = player.getUniqueId();

        // Add player to the active ability set
        activeAbilityPlayers.add(playerId);

        // Create a repeating task to spawn particles around the player during the ability
        BukkitRunnable particleTask = new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                if (counter >= 200) { // 10 seconds = 200 ticks
                    // Stop the particle effect and end the ability
                    activeAbilityPlayers.remove(playerId);
                    this.cancel();
                } else {
                    // Spawn a faint aura of black particles around the player
                    spawnParticleAura(player);
                    counter += 4; // Increment counter by the tick delay between runs (4 ticks per run)
                }
            }
        };
        particleTask.runTaskTimer(plugin, 0L, 4L); // Run every 4 ticks (0.2 seconds)
    }

    private void performTeleportation(Player player) {
        Location originalLocation = player.getLocation();

        // Teleport player 300 blocks up
        Location teleportLocation = originalLocation.clone().add(0, 300, 0);
        player.teleport(teleportLocation);

        // Schedule teleport back after 0.2 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> player.teleport(originalLocation), 4L);
    }

    private void spawnParticleAura(Player player) {
        Location loc = player.getLocation();
        for (int i = 0; i < 20; i++) {
            double angle = i * Math.PI / 10;
            double x = Math.cos(angle) * 1.5;
            double z = Math.sin(angle) * 1.5;
            loc.add(x, 0.5, z);
            player.getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc, 0, 0, 0, 0, 0.1);
            loc.subtract(x, 0.5, z); // Reset location to the player's original location
        }
    }
}
