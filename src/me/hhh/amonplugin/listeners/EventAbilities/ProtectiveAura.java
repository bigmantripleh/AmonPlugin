package me.hhh.amonplugin.listeners.EventAbilities;

import me.hhh.amonplugin.Helpers.PluginHelper;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ProtectiveAura implements Listener {

    private final JavaPlugin plugin;
    private final Set<Player> players;
    private final Map<UUID,Long> cooldowns;

    public ProtectiveAura(JavaPlugin plugin) {
        this.plugin = plugin;
        this.players = new HashSet<>();
        this.cooldowns = new HashMap<>();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();

        if (player.getInventory().getItemInMainHand().getType() != Material.BOOK) {
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

            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§a§lPROTECTIVE AURA IS ON COOLDOWN"));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
            return;
        }
        cooldowns.put(uuid, System.currentTimeMillis() + 20000);
        triggerProtectiveAura(player);
    }


    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if(players.contains(player)) {
            players.remove(player);
            player.setInvulnerable(false);
        }
    }

    private void triggerProtectiveAura(Player player) {

        Location loc = player.getLocation();

        new BukkitRunnable() {
            int ticksPassed = 0;

            @Override
            public void run() {
                if (ticksPassed >= 20) {
                    this.cancel();
                    players.forEach(player -> {
                        player.setInvulnerable(false);
                        player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.5F,1F);
                    });
                    players.clear();
                    return;
                }

                for (Entity entity : loc.getWorld().getNearbyEntities(loc, 5, 1, 5)) {
                    if (entity instanceof Player) {
                        Player otherPlayer = (Player) entity;

                        if(players.contains(otherPlayer)) {
                            continue;
                        }

                        if (PluginHelper.isOnlinePlayer(plugin.getServer(), otherPlayer)) {
                            otherPlayer.setInvulnerable(true);
                            otherPlayer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 10*20, 0));
                            otherPlayer.playSound(otherPlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F,1F);
                            players.add(otherPlayer);
                        }
                    }
                }
                players.forEach(player -> {player.spawnParticle(Particle.HEART, player.getEyeLocation(), 10);});

                // Visualize the border of the aura at feet height
                for (double angle = 0; angle < 360; angle += 5) {
                    double radian = Math.toRadians(angle);
                    double x = Math.cos(radian) * 5;
                    double z = Math.sin(radian) * 5;
                    player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc.clone().add(x, 1, z), 1, 0, 0, 0, 0);
                }

                ticksPassed++;
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }
}
