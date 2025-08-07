package me.hhh.amonplugin.listeners;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.*;

public class MathHealing implements Listener, MathRunnable.ChargeListener {

    private final JavaPlugin plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, Integer> damage = new HashMap<>();
    private Player player = null;
    private final List<ChatListener> chatListers = new ArrayList<>();

    public MathHealing(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void addListener(ChatListener listener) {

        if(listener == null) {
            player.sendMessage("null");
        }

        chatListers.add(listener);
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (player.getInventory().getItemInMainHand().getType() == Material.REDSTONE) {
            ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();

            UUID uuid = player.getUniqueId();
            if (meta != null
                    && meta.hasEnchant(Enchantment.MENDING)
                && meta.getEnchantLevel(Enchantment.MENDING) == 10

            ) {
                if (cooldowns.containsKey(uuid) && cooldowns.get(uuid) > System.currentTimeMillis()) {

                    player.sendMessage(ChatColor.RED + "MathHealing is on cooldown!");
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                    return;
                }

                this.player = player;

                MathRunnable mathRunnable = new MathRunnable(this, player);
                addListener(mathRunnable);
                mathRunnable.runTaskTimer(plugin, 0, 10);
                cooldowns.put(uuid, System.currentTimeMillis() + 20000);
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player chatPlayer = event.getPlayer();

        if (chatPlayer != player) {
            return;
        }

        for (ChatListener chatListener : chatListers) {
            chatListener.chatEvent(event.getMessage());
        }
    }

    @Override
    public void applyCharges(int charges) {
        chatListers.clear();
        Location middle = player.getLocation().clone().add(0, 2, 0);
        Location start1 = middle.clone().add(2.5, 0, 0);
        Location end1 = start1.clone().subtract(5, 0, 0);
        Location start2 = middle.clone().subtract(0, 2.5, 0);
        Location end2 = middle.clone().subtract(0, 2.5, 0);

        middle.getWorld().playSound(middle, Sound.ENTITY_ENDER_DRAGON_SHOOT, 1.0F, 1.0F);
        createParticleLine(start1, end1);
        createParticleLine(start2, end2);
    }

    private void createParticleLine(Location from, Location to) {

        from.setY(from.getY()+1);
        to.setY(to.getY()+1);

        World world = from.getWorld();
        if (world == null || !world.equals(to.getWorld())) {
            return;
        }

        Vector direction = to.toVector().subtract(from.toVector());
        double distance = direction.length();
        direction.normalize();

        double stepSize = 0.1;
        int steps = (int) (distance / stepSize);

        for (int i = 0; i <= steps; i++) {
            Location point = from.clone().add(direction.clone().multiply(i * stepSize));

            Color color = Color.GREEN;

            world.spawnParticle(Particle.REDSTONE, point, 2, new Particle.DustOptions(color, 2.0F));
        }
    }
}
