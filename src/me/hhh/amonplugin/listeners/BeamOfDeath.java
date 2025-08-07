package me.hhh.amonplugin.listeners;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
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
    private final Map<UUID, Boolean> isCharging = new HashMap<>();
    private final Map<UUID, BukkitTask> tasks = new HashMap<>();

    public BeamOfDeath(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        Action action = event.getAction();
        Player player = event.getPlayer();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        UUID uuid = player.getUniqueId();

        if (player.getInventory().getItemInMainHand().getType() == Material.BLAZE_POWDER) {
            ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();

            if (meta != null
                    && meta.hasEnchant(Enchantment.MENDING) && meta.getEnchantLevel(Enchantment.MENDING) == 10
            ) {
                if (isCharging.containsKey(uuid) && !isCharging.get(uuid) && cooldowns.containsKey(uuid) && cooldowns.get(uuid) > System.currentTimeMillis()) {
                    player.sendMessage(ChatColor.RED + "Chain attack is on cooldown!");
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                    return;
                }
                player.playSound(player.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 1.0F, 1.0F);
                totalCharge.put(uuid, 1);
                BukkitTask task = getServer().getScheduler().runTaskTimer(plugin, () -> chargeAttack(player), 0, 10);
                tasks.put(player.getUniqueId(), task);

                isCharging.put(uuid, true);
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
        Integer charges = totalCharge.get(player.getUniqueId());
        world.spawnParticle(Particle.LAVA, playerLocation, 1);

        if (charges == null) {
            return;
        }

        if (charges == 20) {
            return;
        }

        if (charges >= 10) {
            player.damage(1);
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 1.0F, 1.0F);
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
        Integer charges = totalCharge.get(player.getUniqueId());

        if (charges == null) {
            return;
        }
        totalCharge.put(uuid, 0);
        isCharging.put(uuid, false);
        tasks.get(uuid).cancel();
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
                    && meta.hasEnchant(Enchantment.MENDING) && meta.getEnchantLevel(Enchantment.MENDING) == 10
            ) {
                Integer charges = totalCharge.get(uuid);
                player.sendMessage("damage: " + charges);
                tasks.get(uuid).cancel();
                performChargedAttack(player, charges);
                removeCharges(player);

            }
            cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + 20000);
        }
    }

    private void performChargedAttack(Player player, int damage) {

        Location startLocation = player.getEyeLocation();
        Location endLocation = calculateEndLocation(startLocation, 30, player);

        startLocation.getWorld().playSound(startLocation, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
        createLaserAndDamage(startLocation, endLocation, damage);
    }

    private Location calculateEndLocation(Location startLocation, double distance, Player player) {

        double y = startLocation.getY() - 0.5F;
        double z = startLocation.getZ();
        double x = startLocation.getX();

        double pitchRad = Math.toRadians(startLocation.getPitch() * -1F);

        double yaw = startLocation.getYaw();

        if (yaw >= 0) {
            yaw -= 90;
        } else {
            yaw += 90;
        }

        double yawRad = Math.toRadians(yaw);

        double dx = distance * Math.cos(pitchRad) * Math.cos(yawRad);
        double dy = distance * Math.sin(pitchRad);
        double dz = distance * Math.cos(pitchRad) * Math.sin(yawRad);

        return new Location(startLocation.getWorld(), x + dx, y + dy, z + dz);
    }

    private void createLaserAndDamage(Location from, Location to, int damage) {

        World world = from.getWorld();
        if (world == null || !world.equals(to.getWorld())) {
            return;
        }

        Vector direction = to.toVector().subtract(from.toVector());
        double distance = direction.length();
        direction.normalize();

        double stepSize = 0.1;
        int steps = (int) (distance / stepSize);

        Set<LivingEntity> entitiesToDamage = new HashSet<>();

        double radius = 0.1;

        for (int i = 0; i <= steps; i++) {
            Location point = from.clone().add(direction.clone().multiply(i * stepSize));
            List<Entity> entities = (List<Entity>) from.getWorld().getNearbyEntities(point, radius, radius, radius);

            entities.forEach(entity -> {
                if (entity instanceof LivingEntity) {
                    entitiesToDamage.add((LivingEntity) entity);
                }
            });

            Color color = Color.RED;
            if (i % 2 == 0) {
                color = Color.ORANGE;
            }
            world.spawnParticle(Particle.REDSTONE, point, 1, new Particle.DustOptions(color, 1.0F));
        }

        entitiesToDamage.forEach(entity -> {
            entity.damage(damage);
        });
    }
}
