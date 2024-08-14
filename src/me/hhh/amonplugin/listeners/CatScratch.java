package me.hhh.amonplugin.listeners; // Change this to your actual package name

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class CatScratch implements Listener {
    private final Set<Player> cooldownPlayers = new HashSet<>();
    private final Random random = new Random();
    private static final int ABILITY_DURATION = 100; // 5 seconds in ticks
    private static final int COOLDOWN_DURATION = 300; // 15 seconds in ticks

    private Plugin plugin;

    public CatScratch(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        if (event.getAction().toString().contains("RIGHT_CLICK") && event.getItem() != null) {
            Player player = event.getPlayer();
            ItemStack item = event.getItem();

            // Check if the item is shears and has Mending 10
            if (item.getType() == Material.SHEARS && item.containsEnchantment(org.bukkit.enchantments.Enchantment.MENDING)) {
                if (cooldownPlayers.contains(player)) {
                    player.sendMessage("Please wait until you can use Scratch Fury again");
                    return;
                }

                // Play cat sound effect for ability activation
                player.playSound(player.getLocation(), "entity.cat.ambient", 1.0f, 1.0f);

                // Apply Slowness 10 for 5 seconds
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, ABILITY_DURATION, 9));
                cooldownPlayers.add(player);

                // Handle cooldown and healing
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.sendMessage("Your Scratch Fury has ended. Time for a snack!");
                        player.playSound(player.getLocation(), "entity.cat.ambient", 1.0f, 1.0f);
                        healPlayerForItems(player);
                        healPlayerForItems(player);
                        cooldownPlayers.remove(player);
                    }
                }.runTaskLater(plugin, ABILITY_DURATION); // Replace YourPlugin.getInstance() with your plugin's instance

                // Handle cooldown
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.sendMessage("You can now use Scratch Fury again");
                        cooldownPlayers.remove(player);
                    }
                }.runTaskLater(plugin, COOLDOWN_DURATION);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();

        if (cooldownPlayers.contains(player)) {
            event.setDamage(3); // 1.5 hearts = 3 HP

            Entity entity = event.getEntity();
            if (entity instanceof LivingEntity) {
                // Calculate the direction from the entity to the player
                Vector direction = player.getLocation().toVector().subtract(entity.getLocation().toVector()).normalize();
                // Set velocity to pull entity towards the player
                entity.setVelocity(direction.multiply(0.5)); // Adjust the multiplier to control pull strength
            }

            if (random.nextDouble() < 0.5) { // 20% chance
                dropRandomItem(player.getLocation());
            }
        }
    }

    private void dropRandomItem(org.bukkit.Location location) {
        if (random.nextBoolean()) {
            // Drop fish
            location.getWorld().dropItemNaturally(location, new ItemStack(Material.COD));
        } else {
            // Drop random colored wool
            Material[] colors = {Material.WHITE_WOOL, Material.ORANGE_WOOL, Material.MAGENTA_WOOL, Material.LIGHT_BLUE_WOOL, Material.YELLOW_WOOL,
                    Material.LIME_WOOL, Material.PINK_WOOL, Material.GRAY_WOOL, Material.LIGHT_GRAY_WOOL, Material.CYAN_WOOL,
                    Material.PURPLE_WOOL, Material.BLUE_WOOL, Material.BROWN_WOOL, Material.GREEN_WOOL, Material.RED_WOOL, Material.BLACK_WOOL};
            Material color = colors[random.nextInt(colors.length)];
            location.getWorld().dropItemNaturally(location, new ItemStack(color));
        }
        location.getWorld().playSound(location, "entity.cat.ambient", 1.0f, 1.0f);
    }

    private void healPlayerForItems(Player player) {
        int woolCount = 0;
        int fishCount = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) {
                if (item.getType() == Material.COD) {
                    fishCount += item.getAmount();
                } else if (item.getType().toString().endsWith("_WOOL")) {
                    woolCount += item.getAmount();
                }
            }
        }

        // Total heal amount
        int healAmount = woolCount + fishCount;
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double currentHealth = player.getHealth();
        double newHealth = Math.min(currentHealth + healAmount, maxHealth);

        // Apply regular healing
        player.setHealth(newHealth);

        // Calculate excess healing for absorption
        double excessHealing = Math.max(0, (currentHealth + healAmount) - maxHealth);
        if (excessHealing > 0) {
            double absorptionHearts = player.getAbsorptionAmount() + (excessHealing / 2);
            double maxAbsorption = 20.0; // 20 HP, equivalent to 10 hearts
            player.setAbsorptionAmount(Math.min(absorptionHearts, maxAbsorption));
        }

        // Consume items
        player.getInventory().removeItem(new ItemStack(Material.COD, fishCount));
        for (Material woolColor : Material.values()) {
            if (woolColor.toString().endsWith("_WOOL")) {
                player.getInventory().removeItem(new ItemStack(woolColor, woolCount));
            }
        }
    }
}
