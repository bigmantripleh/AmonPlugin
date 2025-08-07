package me.hhh.amonplugin.listeners.EventAbilities;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ExplosiveSpear implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, Long> cooldowns;

    public ExplosiveSpear(JavaPlugin plugin) {
        this.plugin = plugin;
        this.cooldowns = new HashMap<>();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.FIREWORK_ROCKET
            //&& meta.hasEnchant(Enchantment.MENDING)
            //&& item.getEnchantmentLevel(Enchantment.MENDING) == 10
        ) {
            item.setType(Material.TRIDENT);
            ItemMeta meta = item.getItemMeta();
            meta.addEnchant(Enchantment.LOYALTY, 1, true);
            meta.setUnbreakable(true);
            meta.setDisplayName("§6Burning Spear");
            meta.setCustomModelData(482001);
            meta.setLore(Collections.singletonList("Belongs to a Troll"));
            item.setItemMeta(meta);

            player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        cooldowns.remove(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        cooldowns.remove(player.getUniqueId());
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {

        if (event.getEntity() instanceof Trident) {
            ItemStack trident = ((Trident) event.getEntity()).getItem();

            if (event.getEntity().getShooter() instanceof Player) {
                Player player = (Player) event.getEntity().getShooter();
                if (cooldowns.containsKey(player.getUniqueId()) && cooldowns.get(player.getUniqueId()) > System.currentTimeMillis()) {
                    return;
                }
                cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + 10000);
            }

            if (trident.hasItemMeta()) {
                ItemMeta meta = trident.getItemMeta();
                if (meta.getCustomModelData() == 482001) {

                    if (event.getHitEntity() != null) {
                        Entity hitEntity = event.getHitEntity();
                        hitEntity.getWorld().createExplosion(hitEntity.getLocation(), 2.0F, true, false);

                    } else if (event.getHitBlock() != null) {
                        Block hitBlock = event.getHitBlock();
                        hitBlock.getWorld().createExplosion(hitBlock.getLocation(), 2.0F, true, false);
                    }
                }
            }
        }
    }
}
