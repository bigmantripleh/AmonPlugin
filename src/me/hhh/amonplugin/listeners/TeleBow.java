package me.hhh.amonplugin.listeners;

import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TeleBow implements Listener {

    public JavaPlugin plugin;
    private final String TELEBOW_NAME = "TeleBow"; // The name of the custom bow

    public TeleBow(JavaPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();

        if (event.getBow() != null && event.getBow().getItemMeta() != null &&
                event.getBow().getItemMeta().getDisplayName().equals(TELEBOW_NAME) &&
                event.getBow().getEnchantmentLevel(org.bukkit.enchantments.Enchantment.MENDING) == 7) {

            Arrow arrow = (Arrow) event.getProjectile();
            Location startLocation = arrow.getLocation();

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (arrow.isDead() || arrow.isOnGround()) {
                        cancel();
                        return;
                    }

                    if (startLocation.distance(arrow.getLocation()) >= 10) {
                        // Create particle portal and find the nearest living entity excluding the shooter
                        arrow.getWorld().spawnParticle(Particle.PORTAL, arrow.getLocation(), 50);
                        Location portalLocation = arrow.getLocation();
                        arrow.remove();

                        LivingEntity target = getNearestLivingEntity(portalLocation, player);



                        // Reopen portal after 5 seconds and shoot arrow back
                        new BukkitRunnable() {
                            int ticksPassed = 0;
                            double velcounter = 0.05;

                            @Override
                            public void run() {
                                if (ticksPassed >= 100) {
                                    cancel();
                                    return;
                                }

                                arrow.getWorld().spawnParticle(Particle.PORTAL, portalLocation, 50);

                                if (target != null) {
                                    Location targetLocation = target.getLocation();
                                    targetLocation.add(0, 1, 0); // Adjust the target location to the head level of the entity (1 block up)

                                    Vector velocity = targetLocation.toVector().subtract(portalLocation.toVector()).normalize().multiply(arrow.getVelocity().length() * velcounter);

                                    Arrow returnArrow = player.getWorld().spawnArrow(portalLocation, velocity, (float) velocity.length(), 0);

                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            if (returnArrow.isOnGround() && !returnArrow.isDead()) {
                                                target.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 2, true, true, false));
                                                returnArrow.remove();
                                                cancel();
                                            }
                                        }
                                    }.runTaskTimer(plugin, 0L, 1L);
                                }

                                ticksPassed += 2;
                                velcounter+= 0.05;
                            }
                        }.runTaskTimer(plugin, 100L, 2L); // 5 seconds later (100 ticks)

                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
    }

    private LivingEntity getNearestLivingEntity(Location location, Player shooter) {
        List<Entity> entities = location.getWorld().getEntities();
        Optional<LivingEntity> nearestEntity = entities.stream()
                .filter(e -> e instanceof LivingEntity && !e.isDead() && e != shooter)
                .map(e -> (LivingEntity) e)
                .min((e1, e2) -> (int) (e1.getLocation().distance(location) - e2.getLocation().distance(location)));

        return nearestEntity.orElse(null);
    }
}