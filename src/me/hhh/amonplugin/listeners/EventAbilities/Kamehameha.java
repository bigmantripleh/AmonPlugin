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
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class Kamehameha implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, BukkitRunnable> runnableMap = new HashMap<>();
    private final Map<Integer, Float> rad = new HashMap<Integer, Float>();


    public Kamehameha(JavaPlugin plugin) {
        this.plugin = plugin;
        this.rad.put(0, 0.5f);
        this.rad.put(1, 0.75f);
        this.rad.put(2, 0.75f);
        this.rad.put(3, 1.0f);
        this.rad.put(4, 1.0f);
        this.rad.put(5, 1.5f);
        this.rad.put(6, 1.5f);
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {

        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();

        if (player.getInventory().getItemInMainHand().getType() != Material.DIAMOND) {
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
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§b§lKAMEHAMEHA IS ON COOLDOWN"));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
            return;
        }
        cooldowns.put(uuid, System.currentTimeMillis() + 10000);
        chargeAttack(player);
    }

    private void chargeAttack(Player player) {

        BukkitRunnable runnable = new BukkitRunnable() {

            int charges = 0;

            @Override
            public void run() {

                if (charges >= 6) {
                    this.cancel();
                    Location playerLoc = player.getEyeLocation();
                    player.getWorld().playSound(playerLoc, Sound.BLOCK_BEACON_DEACTIVATE, 5.0F, 1.0F);
                    createLaserAndDamage(playerLoc.subtract(0, 1, 0), player);
                    player.getWorld().playSound(playerLoc, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 5.0F, 1.0F);
                }
                drawCircle(player.getEyeLocation().subtract(0, 1, 0), rad.get(charges));
                charges++;
            }
        };
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 5.0F, 1.0F);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 3 * 20, 10));
        runnable.runTaskTimer(this.plugin, 0L, 10L);
        runnableMap.put(player.getUniqueId(), runnable);
    }

    @EventHandler
    public void onPlayerItemHeldChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        removeRunnable(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        removeRunnable(player.getUniqueId());
    }

    private void removeRunnable(UUID uuid) {
        this.runnableMap.remove(uuid);
    }


    private void drawCircle(Location location, float radius) {

        World world = location.getWorld();

        if (world == null) {
            return;
        }

        location.add(location.getDirection().normalize().multiply(1));

        List<Location> locations = new ArrayList<>();

        Location clone = location.clone().add(0, radius / 2, 0);
        double x = location.getX();
        double y = location.getY();
        double cx = clone.getX();
        double cy = clone.getY();

        for (float j = 0; j <= 360; j = j + 20) {
            double radians = Math.toRadians(j);
            double dx = Math.cos(radians) * (cx - x) - Math.sin(radians) * (cy - y) + x;
            double dy = Math.sin(radians) * (cx - x) + Math.cos(radians) * (cy - y) + y;
            Location point = clone.clone();
            point.setY(dy);
            point.setX(dx);

            locations.add(point);

            Location rotPoint = point.clone();
            rotPoint.setX(x);
            double xrot = rotPoint.getX();
            double zrot = rotPoint.getZ();
            double xp = point.getX();
            double zp = point.getZ();

            for (float i = 0; i <= 180; i = i + 20) {
                double radians1 = Math.toRadians(i);
                double rdx = Math.cos(radians1) * (xp - xrot) - Math.sin(radians1) * (zp - zrot) + xrot;
                double rdz = Math.sin(radians1) * (xp - xrot) + Math.cos(radians1) * (zp - zrot) + zrot;
                locations.add(new Location(world, rdx, point.getY(), rdz));
            }
        }

        for (Location l : locations) {
            world.spawnParticle(Particle.REDSTONE, l, 2, new Particle.DustOptions(Color.AQUA, 1.0F));
        }
    }

    private void createLaserAndDamage(Location from, Player player) {

        Vector direction = from.getDirection().normalize();
        World world = from.getWorld();

        double stepSize = 0.1;
        int steps = (int) (30 / stepSize);

        Set<LivingEntity> entitiesToDamage = new HashSet<>();

        double radius = 0.5;

        for (int i = 0; i <= steps; i++) {
            Location point = from.clone().add(direction.clone().multiply(i * stepSize));

            List<Entity> entities = (List<Entity>) from.getWorld().getNearbyEntities(point, radius, radius, radius);

            if(i %0.5 == 0) {
                entities.forEach(entity -> {
                    if (entity instanceof LivingEntity) {
                        entitiesToDamage.add((LivingEntity) entity);
                    }
                });
            }

            Color color;
            if (i % 2 == 0) {
                color = Color.WHITE;
            }else {
                color = Color.AQUA;
            }
            world.spawnParticle(Particle.REDSTONE, point, 2, new Particle.DustOptions(color, 5.0F));
        }
        entitiesToDamage.remove(player);

        entitiesToDamage.forEach(entity -> {
            entity.damage(60);
        });
    }

}


