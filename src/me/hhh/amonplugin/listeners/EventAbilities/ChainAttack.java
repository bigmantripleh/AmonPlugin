package me.hhh.amonplugin.listeners.EventAbilities;

import me.hhh.amonplugin.listeners.PluginHelper;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChainAttack implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public ChainAttack(JavaPlugin plugin) {
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
        if (player.getInventory().getItemInMainHand().getType() == Material.PRISMARINE_SHARD) {
            ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();

            if (meta != null
                //&& meta.hasEnchant(Enchantment.MENDING) && meta.getEnchantLevel(Enchantment.MENDING) == 10
            ) {
                if (cooldowns.containsKey(player.getUniqueId()) && cooldowns.get(player.getUniqueId()) > System.currentTimeMillis()) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§b§lCHAIN ATTACK IS ON COOLDOWN"));
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                    return;
                }
                LivingEntity target = getClosestTarget(player, player, 7.0);
                if (target == null) {
                    return;
                }
                // Trigger the ability
                triggerChainAttack(player, target);

                // Set cooldown
                cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + 6000);
            }
        }
    }

    private LivingEntity getClosestTarget(LivingEntity lastTarget, LivingEntity currentTarget, double maxDistance) {

        Location currentTargetLocation = currentTarget.getLocation();
        LivingEntity closestTarget = null;

        UUID currentTargetUniqueId = currentTarget.getUniqueId();
        UUID lastTargetUniqueId = lastTarget.getUniqueId();

        for (Entity entity : currentTarget.getWorld().getNearbyEntities(currentTargetLocation, maxDistance, maxDistance, maxDistance)) {

            UUID entityUniqueId = entity.getUniqueId();

            if (entity instanceof LivingEntity
                    && !entityUniqueId.equals(currentTargetUniqueId)
                    && !entityUniqueId.equals(lastTargetUniqueId)) {

                if (entity instanceof Player) {
                    if (PluginHelper.isOnlinePlayer(plugin.getServer(), (Player) entity)) {
                        continue;
                    }
                }

                LivingEntity livingEntity = (LivingEntity) entity;
                Location entityLocation = livingEntity.getLocation();

                if (closestTarget == null) {
                    closestTarget = livingEntity;
                }

                if (currentTargetLocation.distanceSquared(entityLocation) < currentTargetLocation.distanceSquared(closestTarget.getLocation())) {
                    closestTarget = livingEntity;
                }
            }
        }
        return closestTarget;
    }

    private void triggerChainAttack(Player player, LivingEntity target) {
        if (target != null) {
            player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5F, 1.0F);
            chainAttack(player, target, 10, 10);
        }
    }

    private void chainAttack(LivingEntity triggerTarget, LivingEntity damageTarget, double damage, int numOfMaxTargets) {

        if (numOfMaxTargets <= 0 || damageTarget == null) {
            return;
        }
        numOfMaxTargets--;
        damageTarget.damage(damage);
        createParticleLine(triggerTarget.getLocation(), damageTarget.getLocation());
        damage = damage * 0.9;
        LivingEntity nextTarget = getClosestTarget(triggerTarget, damageTarget, 4.0);
        chainAttack(damageTarget, nextTarget, damage, numOfMaxTargets);
    }

    private void createParticleLine(Location from, Location to) {

        from.setY(from.getY() + 1);
        to.setY(to.getY() + 1);

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

            Color color = org.bukkit.Color.AQUA;
            if (i % 2 == 0) {
                color = org.bukkit.Color.WHITE;
            }
            world.spawnParticle(Particle.REDSTONE, point, 2, new Particle.DustOptions(color, 1.0F));
        }
    }
}