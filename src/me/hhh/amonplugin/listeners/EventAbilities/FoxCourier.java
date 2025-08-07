package me.hhh.amonplugin.listeners.EventAbilities;

import me.hhh.amonplugin.listeners.PluginHelper;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fox;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FoxCourier implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public FoxCourier(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Check if the action is a right-click

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() == Material.RABBIT_FOOT) {
            ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();

            if (meta != null
                //&& meta.hasEnchant(Enchantment.MENDING) && meta.getEnchantLevel(Enchantment.MENDING) == 10
            ) {
                if (cooldowns.containsKey(player.getUniqueId()) && cooldowns.get(player.getUniqueId()) > System.currentTimeMillis()) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§6§lFOX COURIER IS ON COOLDOWN"));
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                    return;
                }
                // Trigger the ability
                triggerAttack(player);

                // Set cooldown
                cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + 6000);
            }
        }
    }

    public void triggerAttack(Player player){
        Location loc = player.getLocation();
        Fox fox = loc.getWorld().spawn(loc, Fox.class);
        fox.setFirstTrustedPlayer(player);
        fox.setSecondTrustedPlayer(player);
        fox.setFoxType(Fox.Type.RED);
        fox.setAdult();
        fox.getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
    }
}