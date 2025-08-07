package me.hhh.amonplugin.listeners.EventAbilities;

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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

import static org.bukkit.Bukkit.getServer;

public class BeamOfDeath implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, Integer> totalCharge = new HashMap<>();
    private final Map<UUID, BukkitTask> tasks = new HashMap<>();

    public BeamOfDeath(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (player.getInventory().getItemInMainHand().getType() == Material.BLAZE_POWDER) {
            ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();

            if (meta != null
                //&& meta.hasEnchant(Enchantment.MENDING)
                //&& meta.getEnchantLevel(Enchantment.MENDING) == 10

            ) {
                if (cooldowns.containsKey(uuid) && cooldowns.get(uuid) > System.currentTimeMillis()) {

                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§c§lBEAM ATTACK IS ON COOLDOWN"));
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                    return;
                }

                if (tasks.containsKey(uuid)) {
                    return;
                }

                player.playSound(player.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 1.0F, 1.0F);
                totalCharge.put(uuid, 1);
                BukkitTask task = getServer().getScheduler().runTaskTimer(plugin, () -> chargeAttack(player), 0, 20);
                tasks.put(player.getUniqueId(), task);

            }
        }
    }

    public void chargeAttack(Player player) {

        Location playerLocation = player.getLocation();

        World world = playerLocation.getWorld();
        if (world == null) {
            return;
        }

        UUID uuid = player.getUniqueId();
        Integer charges = totalCharge.get(uuid);
        world.spawnParticle(Particle.LAVA, playerLocation, 2);

        if (charges == null) {
            removeCharges(player);
            return;
        }

        if (charges == 10) {
            return;
        }

        if (charges >= 5) {
            player.damage(1);
            world.playSound(playerLocation, Sound.BLOCK_FIRE_AMBIENT, 1.0F, 1.0F);
        }

        totalCharge.put(uuid, charges + 1);

    }

    @EventHandler
    public void onPlayerItemHeldChange(PlayerItemHeldEvent event) {
        removeCharges(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removeCharges(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerQuitEvent event) {
        removeCharges(event.getPlayer());
    }

    private void removeCharges(Player player) {

        UUID uuid = player.getUniqueId();

        totalCharge.put(uuid, 0);

        if (tasks.containsKey(uuid)) {
            tasks.remove(uuid).cancel();
        }
    }

    @EventHandler
    public void onPlayerRelease(PlayerInteractEvent event) {

        Action action = event.getAction();
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        if (!totalCharge.containsKey(uuid) || totalCharge.get(uuid) == 0) {
            return;
        }

        if (player.getInventory().getItemInMainHand().getType() == Material.BLAZE_POWDER) {
            ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();

            if (meta != null
                //&& meta.hasEnchant(Enchantment.MENDING)
                //&& meta.getEnchantLevel(Enchantment.MENDING) == 10
            ) {
                Integer charges = totalCharge.get(uuid);
                performChargedAttack(player, charges);
                removeCharges(player);

            }
            cooldowns.put(uuid, System.currentTimeMillis() + 7000);
        }
    }

    private void performChargedAttack(Player player, int damage) {

        Location startLocation = player.getEyeLocation().subtract(0, 1, 0);

        startLocation.getWorld().playSound(startLocation, Sound.ENTITY_ENDER_DRAGON_SHOOT, 1.0F, 1.0F);
        createLaserAndDamage(startLocation, player, damage);

    }

    private static Vector getPerpendicularVector(Vector v) {
        if (Math.abs(v.getX()) < Math.abs(v.getZ())) {
            return new Vector(1, 0, -v.getX() / v.getZ());
        } else {
            return new Vector(-v.getZ() / v.getX(), 0, 1);
        }
    }

    private void createLaserAndDamage(Location from, Player player, int damage) {

        Vector direction = from.getDirection().normalize();
        World world = from.getWorld();

        double stepSize = 0.125;
        int steps = (int) (30 / stepSize);

        int pointsPerTurn = 32;
        double angleStep = 2 * Math.PI / pointsPerTurn;
        double radius = 0.5;

        for (int i = 0; i <= steps; i++) {

            double angle = i * angleStep;
            double xOffset = Math.cos(angle) * radius;
            double zOffset = Math.sin(angle) * radius;
            double yOffset = i * stepSize;

            Vector perpendicular = getPerpendicularVector(direction).normalize().multiply(xOffset);
            Vector orthogonal = direction.clone().crossProduct(perpendicular).normalize().multiply(zOffset);

            Vector helixPoint = from.toVector()
                    .add(direction.clone().normalize().multiply(yOffset))
                    .add(perpendicular)
                    .add(orthogonal);

            Location point = helixPoint.toLocation(world);

            if (i %4 == 0) {
                List<Entity> entities = (List<Entity>) from.getWorld().getNearbyEntities(point, 0.25, 0.25, 0.25);

                entities.forEach(entity -> {
                    if (entity instanceof LivingEntity) {
                        if (!entity.getUniqueId().equals(player.getUniqueId())) {
                            LivingEntity livingEntity = (LivingEntity) entity;
                            livingEntity.damage(damage, player);
                        }
                    }
                });
            }
            if((i & 1) == 0){
                world.spawnParticle(Particle.REDSTONE, point, 2, new Particle.DustOptions(Color.ORANGE, 1.0F));
            }else{
                world.spawnParticle(Particle.REDSTONE, point, 2, new Particle.DustOptions(Color.RED, 1.0F));
            }
        }
    }
}
