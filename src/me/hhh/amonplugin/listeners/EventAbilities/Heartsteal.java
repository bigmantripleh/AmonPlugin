package me.hhh.amonplugin.listeners.EventAbilities;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class Heartsteal implements Listener {

    private final JavaPlugin plugin;

    private final Map<UUID, BukkitTask> tasks;
    private final Map<UUID, Double> initHealth;
    private final Map<UUID, Double> currentHealth;

    public Heartsteal(JavaPlugin plugin) {
        this.plugin = plugin;
        this.initHealth = new HashMap<>();
        this.currentHealth = new HashMap<>();
        this.tasks = new HashMap<>();
    }

    @EventHandler
    public void EntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();

        if (!(damager instanceof Player)) {
            return;
        }

        EntityDamageEvent.DamageCause cause = event.getCause();

        if (cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            return;
        }

        Player player = (Player) damager;

        if (player.getInventory().getItemInMainHand().getType() != Material.MAGMA_CREAM) {
            return;
        }

        int slot = player.getInventory().getHeldItemSlot();

        ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();

        if (meta == null
            || !meta.hasEnchant(Enchantment.MENDING)
            || meta.getEnchantLevel(Enchantment.MENDING) != 10
        ) {
            return;
        }
        Entity damagee = event.getEntity();

        if (damagee instanceof LivingEntity) {
            triggerHeartsteal(player, (LivingEntity) damagee, slot);
        }
    }

    private void reset(Player player) {
        UUID uuid = player.getUniqueId();
        tasks.remove(uuid);
        if (initHealth.containsKey(uuid)) {
            player.setHealthScale(initHealth.get(uuid));
            initHealth.remove(uuid);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!tasks.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }

        if (event.getItemDrop().getItemStack().getType() == Material.FIREWORK_STAR) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        reset(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        reset(player);
    }

    private void triggerHeartsteal(Player damager, LivingEntity damagee, int slot) {

        UUID uuidDamager = damager.getUniqueId();
        double healthScale = damager.getHealthScale();

        if (!initHealth.containsKey(uuidDamager)) {
            initHealth.put(uuidDamager, healthScale);
        }
        damagee.damage(healthScale);

        if(healthScale < 40){
            damager.setHealthScale(healthScale + 1);
            currentHealth.put(uuidDamager, damager.getHealthScale());
        }
        Location location = damager.getLocation();

        if (location.getWorld() != null) {
            location.getWorld().playSound(location, Sound.BLOCK_ANVIL_LAND, 0.5F, 1.0F);
        }

        damager.getInventory().setItem(slot, new ItemStack(Material.FIREWORK_STAR));

        BukkitTask runnable = new BukkitRunnable() {

            @Override
            public void run() {

                ItemStack magmaCream = new ItemStack(Material.MAGMA_CREAM);
                ItemMeta magmaCreamMeta = magmaCream.getItemMeta();
                magmaCreamMeta.addEnchant(Enchantment.MENDING, 10, true);
                magmaCream.setItemMeta(magmaCreamMeta);

                damager.getInventory().setItem(slot, magmaCream);
                if (location.getWorld() != null) {
                    location.getWorld().playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 0.5F, 1.0F);
                }

                tasks.remove(uuidDamager);
            }
        }.runTaskLater(plugin, 20L * 10L);

        tasks.put(uuidDamager, runnable);
    }
}