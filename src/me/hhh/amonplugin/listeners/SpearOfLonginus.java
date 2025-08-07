package me.hhh.amonplugin.listeners;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Collections;

public class SpearOfLonginus implements Listener {

    private final JavaPlugin plugin;

    public SpearOfLonginus(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.RABBIT
            //&& meta.hasEnchant(Enchantment.MENDING)
            //&& item.getEnchantmentLevel(Enchantment.MENDING) == 10
        ) {
            item.setType(Material.TRIDENT);
            ItemMeta meta = item.getItemMeta();
            meta.addEnchant(Enchantment.DAMAGE_ALL, 3, true);
            meta.addEnchant(Enchantment.LOYALTY, 3, true);
            meta.setUnbreakable(true);
            meta.setDisplayName("§cSpear of Longinus");
            meta.setCustomModelData(672002);
            meta.setLore(Collections.singletonList("LANCEA ET CLAVUS DOMINI"));
            item.setItemMeta(meta);

            player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_BELL_RESONATE, 1, 1);
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {

        if (event.getEntity() instanceof Trident) {
            Entity hitEntity = event.getHitEntity();

            if(hitEntity == null) {
                return;
            }

            ItemStack trident = ((Trident) event.getEntity()).getItem();
            if (trident.hasItemMeta()) {
                ItemMeta meta = trident.getItemMeta();
                if (meta.getCustomModelData() == 672002) {

                    Location entityLocation = hitEntity.getLocation();
                    World world = entityLocation.getWorld();

                    ProjectileSource projectileSource =  event.getEntity().getShooter();

                    if(projectileSource instanceof Player) {
                        Player p1 = (Player) projectileSource;

                        Vector direction = p1.getLocation().toVector().subtract(entityLocation.toVector());
                        direction.multiply(0.5);
                        world.playSound(entityLocation, Sound.ITEM_TRIDENT_THUNDER, 1, 1);
                        hitEntity.setVelocity(direction);

                        if(hitEntity instanceof LivingEntity) {
                            ((LivingEntity) hitEntity).addPotionEffects(Arrays.asList(new PotionEffect(PotionEffectType.SLOW, 2, 2), new PotionEffect(PotionEffectType.SLOW_DIGGING, 2, 2)));
                        }
                    }
                }
            }
        }
    }
}