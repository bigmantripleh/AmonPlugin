package me.hhh.amonplugin.listeners;

import me.hhh.amonplugin.Main;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class BuryTheLight implements Listener {

    HashMap<String, Long> cooldowns = new HashMap<String, Long>();
    ArrayList<Entity> targets = new ArrayList<Entity>();

    boolean dontTNT = false;

    private Main plugin;

    public BuryTheLight(Main plugin) {
        this.plugin = plugin;
    }

    long cooldownTime = 10;

    @EventHandler
    public void BuryTheLight(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getItem() == null) {
            return;
        }

        if (event.getItem().getType() != Material.IRON_SWORD) {
            return;
        }
        player.sendMessage("1");
        if (cooldowns.containsKey(player.getName())) {
            long secondsLeft = ((cooldowns.get(player.getName()) / 1000) + cooldownTime) - (System.currentTimeMillis() / 1000);

            if (secondsLeft > 0) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED + "You cannot do this for another " + secondsLeft + " seconds!"));
                return;
            }
            cooldowns.put(player.getName(), System.currentTimeMillis());

        } else {
            cooldowns.put(player.getName(), System.currentTimeMillis());
            return;
        }
        player.sendMessage("2");
        targets = (ArrayList<Entity>) player.getNearbyEntities(15, 2, 15);
        ArrayList<LivingEntity> OddyList = new ArrayList<LivingEntity>();
        for (int i=0; i<targets.size();i++) {
            if (!(targets.get(i) instanceof LivingEntity)) {
                player.sendMessage("3");
                targets.remove(i);
            } else {
                OddyList.add((LivingEntity) targets.get(i));
            }
        }

        dontTNT = true;
        World world = Bukkit.getServer().getWorld("world");
        for (LivingEntity entity : OddyList) {
            PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, 20 * 5, 3);
            PotionEffect fatigue = new PotionEffect(PotionEffectType.SLOW_DIGGING, 20 * 5, 3);
            PotionEffect slowfalling = new PotionEffect(PotionEffectType.SLOW_FALLING, 20 * 5, 3);
            fatigue.apply(entity);
            slow.apply(entity);
            slowfalling.apply(entity);

            world.strikeLightning(entity.getLocation());
        }
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.instance, new Runnable() {
            @Override
            public void run() {
                for (LivingEntity entity : OddyList) {
                    if (entity.getHealth() - 20 < 0) {
                        TNTPrimed tnt = entity.getWorld().spawn(entity.getLocation(), TNTPrimed.class);
                        tnt.setFuseTicks(1);
                    }
                    entity.damage(20);
                }
            }
        }, 20);
        dontTNT = false;
        targets.clear();
        return;
    }

    @EventHandler
    public void cancelTNT(EntityExplodeEvent event)
    {
        if(dontTNT)
        {
            event.blockList().clear();
        }
        else {
            return;
        }
    }

}

