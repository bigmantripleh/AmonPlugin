package me.hhh.amonplugin.listeners.EventAbilities;

import me.hhh.amonplugin.listeners.PluginHelper;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class BetterBlockThrow implements Listener {

    private final JavaPlugin plugin;
    private final Map<Player, Long> cooldowns = new HashMap<>();
    private final int COOLDOWN_TIME = 5 * 1000;
    private final int TIMEOUT = 20 * 20;
    private ArmorStand armorStand;
    private final Map<Player, BukkitRunnable> tasks = new HashMap<>();

    public BetterBlockThrow(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.NETHERITE_SHOVEL
                //&& meta.hasEnchant(Enchantment.MENDING)
                //&& item.getEnchantmentLevel(Enchantment.MENDING) == 10
        ) {
            if (isOnCooldown(player)) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§n§lBLOCK THROW IS ON COOLDOWN"));
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                return;
            }
            Location loc = player.getLocation().subtract(0,1,0);
            LivingEntity target = getClosestTarget(player, 17.5);

            if (target != null) {
                armorStand = (ArmorStand) target.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
                setupArmorStand(loc.getBlock().getType());
                BetterBlockThrow.ParticleFollower follower = new BetterBlockThrow.ParticleFollower(loc, target, player, armorStand);
                follower.runTaskTimer(plugin, 0L, 2L);
                tasks.put(player, follower);

                cooldowns.put(player, System.currentTimeMillis());
            }
        }
    }

    private void setupArmorStand(Material material) {
        armorStand.setInvisible(true);
        armorStand.setCollidable(false);
        armorStand.setMarker(true);
        armorStand.setGravity(false);
        armorStand.setRotation(0, 0);

        if(material.equals(Material.AIR)) {
            material = Material.DIRT;
        }

        armorStand.getEquipment().setHelmet(new ItemStack(material));
    }

    private void cancelAllTask() {
        armorStand.remove();
        tasks.values().forEach(BukkitRunnable::cancel);
    }

    private class ParticleFollower extends BukkitRunnable {
        private Location particleLocation;
        private final LivingEntity target;
        private final Player player;
        private final double speed = 0.8;
        private int timeElapsed = 0;
        private final ArmorStand armorStand;

        public ParticleFollower(Location startLocation, LivingEntity target, Player player, ArmorStand armorStand) {
            this.particleLocation = startLocation.clone();
            this.target = target;
            this.player = player;
            this.armorStand = armorStand;
        }

        @Override
        public void run() {

            if (timeElapsed > TIMEOUT || target.isDead()) {
                cancelAllTask();
                this.cancel();
                return;
            }

            Location targetLocation = target.getLocation().subtract(0,1,0).clone();

            Vector direction = targetLocation.toVector().subtract(particleLocation.toVector()).normalize();
            double distance = particleLocation.distance(targetLocation);

            if (distance < 1) {
                triggerSpikeDamage(target, direction);
                cancelAllTask();
                this.cancel();
                return;
            }

            particleLocation.add(direction.multiply(speed));

            armorStand.teleport(particleLocation);
            armorStand.setRotation(timeElapsed * 36, 0);

            timeElapsed++;
        }


        private void triggerSpikeDamage(LivingEntity entity, Vector direction) {
            Location entityLocation = entity.getLocation();

            entityLocation.getWorld().playSound(entityLocation, Sound.BLOCK_GRAVEL_BREAK, 1.0f, 1.0f);

            entity.setVelocity(direction.multiply(4));
            entity.damage(20);

            if(entity instanceof Player && !PluginHelper.isH((Player) entity)) {
                return;
            }
            entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 3*20, 2));
        }
    }

    private boolean isOnCooldown(Player player) {
        return cooldowns.containsKey(player) && (System.currentTimeMillis() - cooldowns.get(player)) < COOLDOWN_TIME;
    }

    public static LivingEntity getClosestTarget(Player player, double maxDistance) {
        Location playerLocation = player.getLocation();
        Vector direction = playerLocation.getDirection().normalize();

        double closestDistance = maxDistance;
        LivingEntity closestTarget = null;

        for (Entity entity : player.getWorld().getNearbyEntities(playerLocation, maxDistance, maxDistance, maxDistance)) {
            if (entity instanceof LivingEntity && !entity.getUniqueId().equals(player.getUniqueId())) {
                LivingEntity livingEntity = (LivingEntity) entity;
                Location entityLocation = livingEntity.getLocation();
                Vector toEntity = entityLocation.toVector().subtract(playerLocation.toVector()).normalize();
                double angle = direction.angle(toEntity);

                if (angle < Math.toRadians(30) && playerLocation.distance(entityLocation) < closestDistance) {
                    closestDistance = playerLocation.distance(entityLocation);
                    closestTarget = livingEntity;
                }
            }
        }
        return closestTarget;
    }
}
