package me.hhh.amonplugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ArmorBreak implements Listener {

    private final JavaPlugin plugin;
    private final Random random = new Random();

    public ArmorBreak(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if the item is an anvil with Mending level 10
        if (item.getType() == Material.ANVIL && item.getEnchantmentLevel(Enchantment.MENDING) == 10) {
            Location playerLocation = player.getLocation();

            // Get nearby entities within a radius of 7 blocks
            List<LivingEntity> targets = new ArrayList<>();
            for (Entity entity : player.getWorld().getNearbyEntities(playerLocation, 7, 7, 7)) {
                if (entity instanceof LivingEntity && entity.getUniqueId() != player.getUniqueId()) {
                    targets.add((LivingEntity) entity);
                }
            }

            // Process each target
            for (LivingEntity target : targets) {
                // Calculate the position 2 blocks in front of the target
                Location swordStartLocation = target.getLocation().add(target.getLocation().getDirection().normalize().multiply(2));

                // Calculate direction and rotation to face the target
                Vector directionToTarget = target.getLocation().toVector().subtract(swordStartLocation.toVector()).normalize();
                float yaw = (float) Math.toDegrees(Math.atan2(directionToTarget.getZ(), directionToTarget.getX())) - 90; // Corrected yaw calculation

                // Spawn an invisible armor stand to simulate the sword
                ArmorStand swordStand = (ArmorStand) target.getWorld().spawnEntity(swordStartLocation, EntityType.ARMOR_STAND);
                swordStand.setInvisible(true);
                swordStand.setMarker(true);
                swordStand.setGravity(false);
                swordStand.setRotation(yaw, 0); // Set the yaw to face the target
                swordStand.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));

                // Animate the sword stab
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // Calculate the target location (where the target is at the time of stabbing)
                        Location stabLocation = target.getLocation();

                        // Move the sword towards the target
                        Vector direction = stabLocation.toVector().subtract(swordStartLocation.toVector()).normalize();
                        swordStand.teleport(swordStand.getLocation().add(direction.multiply(0.5)));

                        // Check if the sword has reached the target
                        if (swordStand.getLocation().distance(stabLocation) < 1.0) {
                            // Play hit sound and particle effect
                            target.getWorld().playSound(stabLocation, Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.0f, 1.0f);
                            target.getWorld().spawnParticle(Particle.CRIT, stabLocation, 10);

                            // Attempt to drop a random piece of armor
                            dropRandomArmor(target);

                            // Apply damage to the target
                            target.damage(3, player);

                            // Schedule the removal of the armor stand
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    swordStand.remove();
                                }
                            }.runTaskLater(plugin, 30L); // Remove after 1.5 seconds

                            cancel();
                        }
                    }
                }.runTaskTimer(plugin, 0L, 1L); // Run every tick
            }
        }
    }

    private void dropRandomArmor(LivingEntity target) {
        EntityEquipment equipment = target.getEquipment();
        if (equipment == null) return;

        List<ItemStack> armorPieces = new ArrayList<>();

        // Collect worn armor pieces
        if (equipment.getHelmet() != null && equipment.getHelmet().getType() != Material.AIR) armorPieces.add(equipment.getHelmet());
        if (equipment.getChestplate() != null && equipment.getChestplate().getType() != Material.AIR) armorPieces.add(equipment.getChestplate());
        if (equipment.getLeggings() != null && equipment.getLeggings().getType() != Material.AIR) armorPieces.add(equipment.getLeggings());
        if (equipment.getBoots() != null && equipment.getBoots().getType() != Material.AIR) armorPieces.add(equipment.getBoots());

        // Exit if no armor is worn
        if (armorPieces.isEmpty()) return;

        // Select a random piece of armor to drop
        ItemStack armorToDrop = armorPieces.get(random.nextInt(armorPieces.size()));

        // Remove the armor piece from the target
        if (armorToDrop.equals(equipment.getHelmet())) equipment.setHelmet(null);
        else if (armorToDrop.equals(equipment.getChestplate())) equipment.setChestplate(null);
        else if (armorToDrop.equals(equipment.getLeggings())) equipment.setLeggings(null);
        else if (armorToDrop.equals(equipment.getBoots())) equipment.setBoots(null);

        // Drop the armor piece at the target's location
        Item droppedItem = target.getWorld().dropItemNaturally(target.getLocation(), armorToDrop);
        droppedItem.setPickupDelay(40); // Delay before the item can be picked up again
    }
}
