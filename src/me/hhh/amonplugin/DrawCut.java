package me.hhh.amonplugin;

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
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;

public class DrawCut implements Listener {
    HashMap<String, Long> cooldowns = new HashMap<String, Long>();

    @EventHandler
    public void drawCut(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Action a = e.getAction();



        int cooldownTime = 5;
        if(!cooldowns.containsKey(player.getName()))
        {
            cooldowns.put(player.getName(), System.currentTimeMillis());
            player.sendMessage("added you :D");
            return;
        }


        if (a != Action.RIGHT_CLICK_AIR) {
            return;
        }


        if (e.getItem().getType() == Material.BEDROCK) {
            if(cooldowns.containsKey(player.getName()))
            {
                long secondsLeft = ((cooldowns.get(player.getName())/1000)+cooldownTime) - (System.currentTimeMillis()/1000);

                if(secondsLeft>0) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED+"You cannot do this for another "+secondsLeft+" seconds!"));
                    return;
                }
                cooldowns.put(player.getName(), System.currentTimeMillis());
            }
            dashFwd(player);
            playDashSoundEffect(player);

            List<Entity> nearby = player.getNearbyEntities(2, 0, 2);
            for (Entity entity : nearby) {
                if (entity instanceof LivingEntity) {
                    LivingEntity le = (LivingEntity) entity;
                    le.damage(10);
                }
            }

        }

    }

    public void dashFwd(Player player) {
        Location location = player.getLocation();
        Vector direction = location.getDirection();
        direction.normalize();
        direction.multiply(5);
        location.add(direction);
        location.setY(Math.floor(location.getY()));

        Vector yVector = new Vector(0, 1, 0);
        Location test1 = location.add(yVector);
        Location test2 = test1.add(yVector);

        World world = Bukkit.getServer().getWorld("world");
        if (test1.getBlock().isEmpty() && test2.getBlock().isEmpty()) {
            if (location.getBlock().isEmpty()) {
                location.setY(location.getY() - 1);
                if (!location.getBlock().isEmpty()) {
                    location.setY(location.getY() + 1);
                    player.teleport(location);
                    return;
                }
                player.teleport(location);
            } else if (test1.getBlock().isEmpty()) {
                player.teleport(test1);
            }
        }
    }

    public void playDashSoundEffect(Player player) {

        World world = player.getWorld();
        Location location = player.getLocation();
        world.playEffect(location, Effect.ANVIL_LAND, 0);

    }

}
