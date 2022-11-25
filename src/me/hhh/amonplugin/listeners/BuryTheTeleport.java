package me.hhh.amonplugin.listeners;

import me.hhh.amonplugin.Main;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class BuryTheTeleport implements Listener {

    ArrayList<Entity> targets = new ArrayList<Entity>();

    boolean dontTNT = false;

    private Main plugin;

    public BuryTheTeleport(Main plugin) {
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

        if (event.getItem().getType() != Material.WOODEN_SWORD) {
            return;
        }

        if(!player.getName().equals("LowerCaseH"))
        {
            return;
        }

        targets = (ArrayList<Entity>) player.getNearbyEntities(50, 5, 50);
        ArrayList<LivingEntity> OddyList = new ArrayList<LivingEntity>();
        for (int i=0; i<targets.size();i++) {
            if (!(targets.get(i) instanceof LivingEntity)) {
                targets.remove(i);
            } else {
                if(!(targets.get(i) instanceof Player))
                {
                    OddyList.add((LivingEntity) targets.get(i));
                }
            }
        }


        World world = Bukkit.getServer().getWorld("world");
        for (LivingEntity entity : OddyList) {
            PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, 20 * 5, 3);
            PotionEffect fatigue = new PotionEffect(PotionEffectType.SLOW_DIGGING, 20 * 5, 3);
            PotionEffect slowfalling = new PotionEffect(PotionEffectType.SLOW_FALLING, 20 * 5, 3);
            fatigue.apply(entity);
            slow.apply(entity);
            slowfalling.apply(entity);
        }
        ArrayList<Location> Locations = new ArrayList<Location>();
        for(Entity entity : OddyList)
        {
            Locations.add(entity.getLocation());
        }
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.instance, new Runnable() {
            @Override
            public void run() {
                for (LivingEntity entity : OddyList) {
                    if(!(entity instanceof Player))
                    {
                        int index = (int)(Math.random() * OddyList.size());
                        entity.teleport(Locations.get(index));
                    }
                }
            }
        }, 20);
        targets.clear();
        return;
    }


}

