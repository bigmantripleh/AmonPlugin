package me.hhh.amonplugin.listeners.EventAbilities;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class CursedAxe implements Listener {

    private final JavaPlugin plugin;
    private final List<UUID> cursedPlayers;
    private final Map<UUID, BukkitTask> cursedTasks;
    private final Map<UUID, Long> cooldowns;

    public CursedAxe(JavaPlugin plugin) {
        this.plugin = plugin;
        this.cursedPlayers = new ArrayList<>();
        this.cursedTasks = new HashMap<>();
        this.cooldowns = new HashMap<>();
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        UUID uuid = player.getUniqueId();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.WITHER_ROSE
            //&& meta.hasEnchant(Enchantment.MENDING)
            //&& item.getEnchantmentLevel(Enchantment.MENDING) == 10
        ) {

            if (cursedPlayers.contains(uuid)) {
                return;
            }

            item.setType(Material.NETHERITE_AXE);
            ItemMeta meta = item.getItemMeta();
            meta.addEnchant(Enchantment.DAMAGE_ALL, 3, true);
            meta.setUnbreakable(true);
            meta.setDisplayName("§4Chopper");
            meta.setLore(Collections.singletonList("Axe of an Undead Juggernaut"));
            item.setItemMeta(meta);

            cursedPlayers.add(uuid);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§4§lRest is for the Living!"));
            Location location = player.getLocation();

            if (location.getWorld() != null) {
                location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EAT, 1.0F, 1.0F);
            }
        }
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (cursedPlayers.contains(uuid)) {
            cursedPlayers.remove(uuid);
            cursedTasks.remove(uuid).cancel();
            cooldowns.remove(uuid);
            player.setInvulnerable(false);

            AttributeInstance atr = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
            if (atr != null) {
                atr.setBaseValue(4.0);
            }

            player.getInventory().forEach(item -> {
                if (item.getType() == Material.NETHERITE_AXE) {
                    item.setType(Material.WITHER_ROSE);
                }
            });
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        Player player = event.getEntity();
        UUID uuid = player.getUniqueId();
        player.setInvulnerable(false);


        if (!cursedPlayers.contains(uuid)) {
            return;
        }

        if (cooldowns.containsKey(uuid) && cooldowns.get(uuid) > System.currentTimeMillis()) {
            return;
        }

        cooldowns.put(uuid, System.currentTimeMillis() + 1000 * 60 * 3);
        Location playerLocation = player.getLocation();
        new BukkitRunnable() {
            @Override
            public void run() {
                player.sendMessage("respawning");
                if (player.isOnline()) {
                    player.setHealth(player.getHealthScale());
                    //player.teleport(playerLocation);
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§4§lLEAVE NO STONE UNBROKEN!"));
                    playerLocation.getWorld().playSound(playerLocation, Sound.ENTITY_WITHER_SPAWN, 1.0F, 1.0F);
                    player.setInvulnerable(true);

                    AttributeInstance atr = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
                    if (atr != null) {
                        atr.setBaseValue(1000);
                    }

                    player.addPotionEffects(Arrays.asList(
                            new PotionEffect(PotionEffectType.WITHER, 60000, 3),
                            new PotionEffect(PotionEffectType.SPEED, 60000, 3),
                            new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 60000, 3),
                            new PotionEffect(PotionEffectType.SATURATION, 60000, 1)));
                }
                BukkitTask bukkitRunnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.setInvulnerable(false);
                        for (PotionEffect effect : player.getActivePotionEffects()){
                            player.removePotionEffect(effect.getType());
                        }

                        AttributeInstance atr = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
                        if (atr != null) {
                            atr.setBaseValue(4.0);
                        }

                        player.addPotionEffects(Arrays.asList(
                                new PotionEffect(PotionEffectType.WITHER, 60000, 3),
                                new PotionEffect(PotionEffectType.HUNGER, 60000, 1),
                                new PotionEffect(PotionEffectType.CONFUSION, 60000, 1)));
                    }
                }.runTaskLater(plugin, 20*10);
                cursedTasks.put(player.getUniqueId(), bukkitRunnable);
            }
        }.runTaskLater(plugin, 1);
    }
}
