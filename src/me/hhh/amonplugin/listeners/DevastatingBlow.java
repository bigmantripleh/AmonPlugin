package me.hhh.amonplugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DevastatingBlow implements Listener {

    private final JavaPlugin plugin;

    public DevastatingBlow(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if the player is holding a Nether Star with Mending 15
        if (item.getType() == Material.NETHER_STAR && item.getItemMeta().hasEnchant(org.bukkit.enchantments.Enchantment.MENDING)
                && item.getItemMeta().getEnchantLevel(org.bukkit.enchantments.Enchantment.MENDING) == 15) {

            // Teleport the player
            Location teleportLocation = new Location(player.getWorld(), 60482, 243, -34154);
            player.teleport(teleportLocation);

            // Cover the player in yellow particles
            player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, teleportLocation, 100, 1, 1, 1, 0.1);

            // Define the area to select 300 blocks from
            Location corner1 = new Location(player.getWorld(), 60462, 242, -34174);
            Location corner2 = new Location(player.getWorld(), 60502, 242, -34134);
            List<Location> possibleLocations = new ArrayList<>();

            for (int x = Math.min(corner1.getBlockX(), corner2.getBlockX()); x <= Math.max(corner1.getBlockX(), corner2.getBlockX()); x++) {
                for (int z = Math.min(corner1.getBlockZ(), corner2.getBlockZ()); z <= Math.max(corner1.getBlockZ(), corner2.getBlockZ()); z++) {
                    possibleLocations.add(new Location(player.getWorld(), x, corner1.getBlockY() + 1, z)); // Adjust Y to be above the block
                }
            }

            // Randomly select 300 blocks
            Random random = new Random();
            List<Location> selectedLocations = new ArrayList<>();
            for (int i = 0; i < 250 && !possibleLocations.isEmpty(); i++) {
                Location selectedLocation = possibleLocations.remove(random.nextInt(possibleLocations.size()));
                selectedLocations.add(selectedLocation);
            }

            // Cover the top of the selected blocks with blue particles and strike lightning
            new BukkitRunnable() {
                int count = 0;

                @Override
                public void run() {
                    if (count >= 100) {  // 60 ticks = 3 seconds
                        this.cancel();
                        return;
                    }

                    for (Location loc : selectedLocations) {
                        player.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, loc, 20, 0.5, 0, 0.5, 0);  // Adjusted to stay at block level
                    }

                    count++;
                }
            }.runTaskTimer(plugin, 0L, 1L);

            // Strike lightning at the selected locations after 3 seconds
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Location loc : selectedLocations) {
                        player.getWorld().strikeLightning(loc); // Use strikeLightningEffect to keep the strike at the ground level
                    }
                }
            }.runTaskLater(plugin, 60L);  // 60 ticks = 3 seconds
        }
    }
}
