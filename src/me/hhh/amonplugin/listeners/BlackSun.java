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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class BlackSun implements Listener {

    private final JavaPlugin plugin;

    public BlackSun(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        Action action = event.getAction();

        // Check if right-click action and if the item is Black Wool with Mending 10
        if ((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) &&
                player.getInventory().getItemInMainHand().getType() == Material.BLACK_WOOL) {

            ItemStack item = player.getInventory().getItemInMainHand();
            ItemMeta meta = item.getItemMeta();

            if (meta != null && meta.hasEnchant(Enchantment.MENDING) &&
                    meta.getEnchantLevel(Enchantment.MENDING) == 10) {

                Location center = player.getEyeLocation();

                // Run particle effect and entity pulling logic
                runBlackSunEffect(center);
            }
        }
    }

    private void runBlackSunEffect(Location center) {
        final World world = center.getWorld();
        if (world == null) {
            return;
        }

        // Increase the Y coordinate for the center to align particles above ground
        center.setY(center.getY() + 1);

        new BukkitRunnable() {

            int timer = 10 * 20; // 10 seconds (20 ticks per second)

            @Override
            public void run() {
                if (timer <= 0) {
                    this.cancel();
                    return;
                }
                timer -= 4; // Every 0.2 seconds (4 ticks)

                createParticles(center, 5);
                pullEntitiesTowardsCenter(center, world);
            }
        }.runTaskTimer(plugin, 0L, 4L);
    }

    private void createParticles(Location location, double radius) {
        final World world = location.getWorld();
        if (world == null) {
            return;
        }

        final Location center = location.clone();
        final Location point = center.clone();

        center.setY(location.getY() + radius);
        point.setY(center.getY() + radius);

        double x1 = point.getX();
        double y1 = point.getY();
        double x2 = center.getX();
        double y2 = center.getY();
        double z = center.getZ();

        new BukkitRunnable() {
            @Override
            public void run() {
                boolean toggle = true;
                Particle.DustOptions color = new Particle.DustOptions(Color.BLACK, 1.0F);

                for (double angle = 0; angle < 360; angle += 2.25) {

                    double radians = Math.toRadians(angle);

                    double xDeltaBig = calcX(x1, x2, y1, y2, radians);
                    double yDeltaBig = calcY(x1, x2, y1, y2, radians);

                    double y1s = point.getY() - (radius * 0.75);

                    double xDeltaSmall = calcX(x1, x2, y1s, y2, radians);
                    double yDeltaSmall = calcY(x1, x2, y1s, y2, radians);

                    Location locBig = new Location(world, xDeltaBig, yDeltaBig, z);
                    Location locSmall = new Location(world, xDeltaSmall, yDeltaSmall, z);
                    Location locMid = null;

                    world.spawnParticle(Particle.REDSTONE, locBig, 2, color);
                    world.spawnParticle(Particle.REDSTONE, locSmall, 1, color);

                    if (!toggle) {
                        double y1m = point.getY() - (radius * 0.25);
                        double xDeltaMid = calcX(x1, x2, y1m, y2, radians);
                        double yDeltaMid = calcY(x1, x2, y1m, y2, radians);
                        locMid = new Location(world, xDeltaMid, yDeltaMid, z);

                        world.spawnParticle(Particle.REDSTONE, locMid, 1, color);
                    }

                    if (angle % 22.5 == 0) {
                        if (toggle) {
                            double y1m = point.getY() - (radius * 0.25);
                            double xDeltaMid = calcX(x1, x2, y1m, y2, radians);
                            double yDeltaMid = calcY(x1, x2, y1m, y2, radians);
                            locMid = new Location(world, xDeltaMid, yDeltaMid, z);

                            createParticleLine(center, locMid, world, color);

                        } else {
                            createParticleLine(locMid, locBig, world, color);
                        }
                        toggle = !toggle;
                    }
                }
            }
        }.runTask(plugin);
    }

    private void pullEntitiesTowardsCenter(Location center, World world) {
        List<Entity> nearbyEntities = world.getEntities();

        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity && entity.getLocation().distance(center) <= 20) {
                // Check if the entity is not the player named "LowerCaseH"
                if (entity instanceof Player && "LowerCaseH".equals(entity.getName())) {
                    continue; // Skip this entity if it is the player named "LowerCaseH"
                }

                // Calculate direction towards the center and apply pulling force
                Vector direction = center.toVector().subtract(entity.getLocation().toVector()).normalize();
                entity.setVelocity(direction.multiply(0.5));

                // Check if the entity is at the center
                if (entity.getLocation().distance(center) < 1.5) {
                    Location originalLocation = entity.getLocation().clone(); // Store original location
                    entity.teleport(new Location(world, 60280, 24, -34139)); // Teleport to distant location

                    // Schedule teleportation back after 10 seconds (200 ticks)
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            entity.teleport(originalLocation);
                        }
                    }.runTaskLater(plugin, 200L);
                }
            }
        }
    }



    private void createParticleLine(Location from, Location to, World world, Particle.DustOptions color) {
        Vector direction = to.toVector().subtract(from.toVector()).normalize();
        double distance = from.distance(to);
        double stepSize = 0.25;
        int steps = (int) (distance / stepSize);

        for (int i = 0; i <= steps; i++) {
            Location point = from.clone().add(direction.clone().multiply(i * stepSize));
            world.spawnParticle(Particle.REDSTONE, point, 1, color);
        }
    }

    private double calcX(double x1, double x2, double y1, double y2, double radians) {
        return (x1 - x2) * Math.cos(radians) - (y1 - y2) * Math.sin(radians) + x2;
    }

    private double calcY(double x1, double x2, double y1, double y2, double radians) {
        return (x1 - x2) * Math.sin(radians) + (y1 - y2) * Math.cos(radians) + y2;
    }
}
