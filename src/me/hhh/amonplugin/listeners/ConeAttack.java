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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

public class ConeAttack implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public ConeAttack(JavaPlugin plugin) {
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

        if (player.getInventory().getItemInMainHand().getType() == Material.SUGAR) {
            ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();

            if (meta != null
                && meta.hasEnchant(Enchantment.MENDING)
                && meta.getEnchantLevel(Enchantment.MENDING) == 10

            ) {
                if (cooldowns.containsKey(uuid) && cooldowns.get(uuid) > System.currentTimeMillis()) {

                    player.sendMessage(ChatColor.BLUE + "Cone attack is on cooldown!");
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                    return;
                }
                cooldowns.put(uuid, System.currentTimeMillis() + 1000);
                performConeAttack(player.getEyeLocation(), calculateEndLocations(player.getEyeLocation(), 20), player);
            }
        }
    }

    private List<Location> calculateEndLocations(Location startLocation, double distance) {

        startLocation = startLocation.clone();
        startLocation.subtract(0, 1, 0);
        double y = startLocation.getY();
        double z = startLocation.getZ();
        double x = startLocation.getX();

        double pitchRad = Math.toRadians(startLocation.getPitch() * -1F);

        double yaw = startLocation.getYaw() + 90;

        double yawRad = Math.toRadians(yaw);

        double dx = distance * Math.cos(pitchRad) * Math.cos(yawRad);
        double dy = distance * Math.sin(pitchRad);
        double dz = distance * Math.cos(pitchRad) * Math.sin(yawRad);

        Location endLocation = new Location(startLocation.getWorld(), x + dx, y + dy, z + dz);

        List<Location> endLocations = new ArrayList<>();

        for (int i = 0; i < 16; i++) {

            double randX = (Math.random() * 30.0) - 15;
            double randY = (Math.random() * 5);
            double randZ = (Math.random() * 30.0) - 15;

            endLocations.add(endLocation.clone().add(randX, randY, randZ));
        }

        return endLocations;
    }

    private void performConeAttack(Location from, List<Location> to, Player player) {

        World world = from.getWorld();
        if (world == null) {
            return;
        }

        List<Vector> directions = new ArrayList<>();
        double distance = 20.0;

        for (Location location : to) {
            Vector direction = location.toVector().subtract(from.toVector());
            direction.normalize();
            directions.add(direction);
        }

        double stepSize = 0.1;
        int steps = (int) (distance / stepSize);

        Set<Entity> damagedEntities = new HashSet<>();
        damagedEntities.add(player);
        Color[] colors = new Color[]{Color.BLUE, Color.GREEN, Color.RED, Color.YELLOW, Color.PURPLE, Color.ORANGE, Color.WHITE};

        for (int i = 0; i <= steps; i++) {
            Color color = colors[i % 7];
            for (int j = 0; j < directions.size(); j++) {
                try {
                    Location point = from.clone().add(directions.get(j).clone().multiply(i * stepSize));

                    List<Entity> entityList = (List<Entity>) from.getWorld().getNearbyEntities(point, .1F, .1F, .1F);

                    for (Entity entity : entityList) {
                        if (!damagedEntities.contains(entity)) {
                            if (entity instanceof LivingEntity) {
                                List<PotionEffect> potionEffects = Arrays.asList(new PotionEffect(PotionEffectType.CONFUSION, 100, 1), new PotionEffect(PotionEffectType.SPEED, 100, 1), new PotionEffect(PotionEffectType.POISON, 100, 1));
                                ((LivingEntity) entity).addPotionEffects(potionEffects);
                                damagedEntities.add(entity);
                            }
                        }
                    }

                    world.spawnParticle(Particle.REDSTONE, point, 1, new Particle.DustOptions(color, 1.0F));
                } catch (Exception e) {
                    //Konsole ist eh viel zu voll 😁
                }
            }
        }
    }
}
