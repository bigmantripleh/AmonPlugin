package me.hhh.amonplugin.listeners;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class BlackSun implements Listener {

    private final JavaPlugin plugin;

    public BlackSun(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (player.getInventory().getItemInMainHand().getType() != Material.STICK) {
            return;
        }
        ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();

        if (meta != null
                && meta.hasEnchant(Enchantment.MENDING)
            // && meta.getEnchantLevel(Enchantment.MENDING) == 10

        ) {
            createParticles(player.getEyeLocation(), 5);
        }
    }

    private double calcX(double x1, double x2, double y1, double y2, double radians) {
        return (x1 - x2) * Math.cos(radians) - (y1 - y2) * Math.sin(radians) + x2;
    }

    private double calcY(double x1, double x2, double y1, double y2, double radians) {
        return (x1 - x2) * Math.sin(radians) + (y1 - y2) * Math.cos(radians) + y2;
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

            int timeout = 60; // 60 sec timer
            Particle.DustOptions color = new Particle.DustOptions(Color.BLACK, 1.0F);

            @Override
            public void run() {

                //cancel if time is up
                if (timeout <= 0) {
                    this.cancel();
                }
                timeout--;

                //used for drawing lines
                boolean toggle = true;

                for (double angle = 0; angle < 360; angle += 2.25) {

                    double radians = Math.toRadians(angle);

                    // outer big circle
                    double xDeltaBig = calcX(x1, x2, y1, y2, radians);
                    double yDeltaBig = calcY(x1, x2, y1, y2, radians);

                    double y1s = point.getY() - (radius * 0.75); // different radius

                    // inner circle
                    double xDeltaSmall = calcX(x1, x2, y1s, y2, radians);
                    double yDeltaSmall = calcY(x1, x2, y1s, y2, radians);

                    double xDeltaMid = 0;
                    double yDeltaMid = 0;

                    Location locBig = new Location(world, xDeltaBig, yDeltaBig, z);
                    Location locSmall = new Location(world, xDeltaSmall, yDeltaSmall, z);
                    Location locMid = null;

                    world.spawnParticle(Particle.REDSTONE, locBig, 2, color);
                    world.spawnParticle(Particle.REDSTONE, locSmall, 1, color);

                    //interrupted circle for lines
                    if (!toggle) {

                        double y1m = point.getY() - (radius * 0.25);

                        xDeltaMid = calcX(x1, x2, y1m, y2, radians);
                        yDeltaMid = calcY(x1, x2, y1m, y2, radians);
                        locMid = new Location(world, xDeltaMid, yDeltaMid, z);

                        world.spawnParticle(Particle.REDSTONE, locMid, 1, color);
                    }

                    //draw lines
                    if (angle % 11.25 == 0) {

                        if (toggle) {
                            //line from center to interrupted circle
                            double y1m = point.getY() - (radius * 0.25); // different radius

                            xDeltaMid = calcX(x1, x2, y1m, y2, radians);
                            yDeltaMid = calcY(x1, x2, y1m, y2, radians);
                            locMid = new Location(world, xDeltaMid, yDeltaMid, z);

                            createParticleLine(center, locMid, world, color);

                        } else {
                            //line from interrupted circle to outer circle
                            createParticleLine(locMid, locBig, world, color);
                        }
                        toggle = !toggle;
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 4L);
    }

    private void createParticleLine(Location from, Location to, World world, Particle.DustOptions color) {

        Vector direction = to.toVector().subtract(from.toVector());
        double distance = direction.length();
        direction.normalize();

        double stepSize = 0.25;
        int steps = (int) (distance / stepSize);

        for (int i = 0; i <= steps; i++) {
            Location point = from.clone().add(direction.clone().multiply(i * stepSize));
            world.spawnParticle(Particle.REDSTONE, point, 1, color);
        }
    }
}

