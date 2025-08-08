package me.hhh.amonplugin.listeners.EventAbilities;

import me.hhh.amonplugin.Helpers.PluginHelper;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class SunfireCape implements Listener {

    private final JavaPlugin plugin;

    private final Map<UUID, Long> cooldowns;
    private final Map<UUID, BukkitTask> tasks;

    public SunfireCape(JavaPlugin plugin) {
        this.plugin = plugin;

        this.cooldowns = new HashMap<>();
        this.tasks = new HashMap<>();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();

        if (player.getInventory().getItemInMainHand().getType() != Material.HONEYCOMB) {
            return;
        }

        ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();

        if (meta == null
            //|| !meta.hasEnchant(Enchantment.MENDING)
            //|| meta.getEnchantLevel(Enchantment.MENDING) != 10
        ) {
            return;

        }

        UUID uuid = player.getUniqueId();

        if (cooldowns.containsKey(uuid) && cooldowns.get(uuid) > System.currentTimeMillis()) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§6§lSUNFIRE CAPE IS ON COOLDOWN"));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
            return;
        }
        cooldowns.put(uuid, System.currentTimeMillis() + 20000);
        triggerSunfireCape(player);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        tasks.remove(player.getUniqueId());
    }


    private void triggerSunfireCape(Player player) {

        World world = player.getWorld();

        BukkitTask runnable = new BukkitRunnable() {
            int ticksPassed = 0;

            @Override
            public void run() {
                if (ticksPassed >= 10) {
                    this.cancel();
                    return;
                }

                Location loc = player.getLocation().add(0, 0.3, 0);
                world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_SHOOT, 1, 1);

                for (Entity entity : world.getNearbyEntities(loc, 5, 1, 5)) {
                    if (entity instanceof Player) {
                        Player otherPlayer = (Player) entity;

                        if (PluginHelper.isOnlinePlayer(plugin.getServer(), otherPlayer)) {
                            continue;
                        }
                    }
                    if (entity instanceof LivingEntity) {
                        ((LivingEntity) entity).damage(2);
                    }
                }

                // Visualize the border of the aura at feet height

                for (double angle = 0; angle < 360; angle += 5) {
                    double radian = Math.toRadians(angle);
                    double x = Math.cos(radian);
                    double z = Math.sin(radian);
                    world.spawnParticle(Particle.FLAME, loc, 0, x, 0, z, 0.1F);
                    world.spawnParticle(Particle.FLAME, loc, 0, x, 0, z, 0.2F);
                }

                ticksPassed++;
            }
        }.runTaskTimer(plugin, 0L, 40L);

        tasks.put(player.getUniqueId(), runnable);
    }
}