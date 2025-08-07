package me.hhh.amonplugin.listeners.EventAbilities;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Cat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CatSummoner implements Listener {

    private JavaPlugin plugin;

    // Store cooldowns and active cats
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, Set<Cat>> activeCats = new HashMap<>();

    public CatSummoner(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if the item is a wooden sword with Mending level 10
        if (item.getType() == Material.WOODEN_SWORD && item.getEnchantmentLevel(Enchantment.MENDING) == 10) {
            // Check cooldown
            if (isOnCooldown(player)) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§6§lCAT SUMMONER IS ON COOLDOWN"));
                return;
            }

            // Spawn cats
            spawnCats(player);
        }
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        // Check if the attacker is a player
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();
        LivingEntity target = (LivingEntity) event.getEntity();

        // Ensure the target is not the player or another cat
        if (target instanceof Player && target.getUniqueId().equals(player.getUniqueId())) {
            return;
        }

        // Command cats to attack the same target
        commandCatsToAttack(player, target);
    }

    private boolean isOnCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        if (!cooldowns.containsKey(playerId)) {
            return false;
        }
        long timeLeft = (cooldowns.get(playerId) - System.currentTimeMillis()) / 1000;
        return timeLeft > 0;
    }

    private void spawnCats(Player player) {
        UUID playerId = player.getUniqueId();
        Location playerLocation = player.getLocation();

        // Create a set to store active cats
        Set<Cat> cats = new HashSet<>();

        // Summon 10 cats
        for (int i = 0; i < 10; i++) {
            Cat cat = (Cat) playerLocation.getWorld().spawnEntity(playerLocation, EntityType.CAT);
            cat.setOwner(player);
            cat.setCustomName("§6Summoned Cat");
            cat.setCustomNameVisible(true);
            cat.setAdult();
            cat.setCollarColor(org.bukkit.DyeColor.ORANGE); // Optional: Set collar color
            cat.setCatType(Cat.Type.ALL_BLACK); // Optional: Set cat type

            cats.add(cat);
        }


        // Play sound effect
        player.getWorld().playSound(playerLocation, Sound.ENTITY_CAT_AMBIENT, 1.0f, 1.0f);

        // Store active cats
        activeCats.put(playerId, cats);

        // Start a task to despawn cats after 15 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                despawnCats(player);
            }
        }.runTaskLater(plugin, 15 * 20L); // 15 seconds (20 ticks per second)

        // Put the ability on cooldown for 45 seconds
        cooldowns.put(playerId, System.currentTimeMillis() + (45 * 1000L));
    }

    private void commandCatsToAttack(Player player, LivingEntity target) {
        UUID playerId = player.getUniqueId();

        if (!activeCats.containsKey(playerId)) {
            return;
        }

        Set<Cat> cats = activeCats.get(playerId);
        for (Cat cat : cats) {
            if (!cat.isDead()) {
                cat.setTarget(target); // Command cat to attack the player's target
                attackTarget(cat, target);
            }
        }
    }

    private void despawnCats(Player player) {
        UUID playerId = player.getUniqueId();

        if (!activeCats.containsKey(playerId)) {
            return;
        }

        Set<Cat> cats = activeCats.get(playerId);
        for (Cat cat : cats) {
            if (!cat.isDead()) {
                cat.remove();
            }
        }

        activeCats.remove(playerId);
        player.sendMessage("§aYour summoned cats have returned to the wild.");
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§6§lCATS HAVE RETURNED TO THE WILD"));
    }

    private void attackTarget(LivingEntity attacker, LivingEntity target) {
        // Attack logic for cats
        if (target != null && !target.isDead()) {
            Vector direction = target.getLocation().toVector().subtract(attacker.getLocation().toVector()).normalize();
            attacker.setVelocity(direction.multiply(0.3)); // Small leap towards target

            // Deal damage
            target.damage(2, attacker); // Adjust damage as needed
        }
    }
}
