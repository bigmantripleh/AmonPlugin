package me.hhh.amonplugin.listeners;

import me.hhh.amonplugin.Main;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_16_R3.CommandPlaySound;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

public class DrawCut implements Listener {
    HashMap<String, Long> cooldowns = new HashMap<String, Long>();

    private Main plugin;
    public boolean droppable = true;
    public DrawCut(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void drawCut(PlayerInteractEvent e) {
        if(plugin.drawcut==false)
        {
            return;
        }
        Player player = e.getPlayer();
        Action a = e.getAction();

        if (a != Action.RIGHT_CLICK_AIR) {
            return;
        }

        int cooldownTime = 5;

        if((e.getItem()).getType() == Material.STICK && e.getItem().getItemMeta().hasCustomModelData() &&(e.getItem().getItemMeta()).getCustomModelData() == 1001){
            if(cooldowns.containsKey(player.getName()))
            {
                long secondsLeft = ((cooldowns.get(player.getName())/1000)+cooldownTime) - (System.currentTimeMillis()/1000);

                if(secondsLeft>0) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED+"You cannot do this for another "+secondsLeft+" seconds!"));
                    return;
                }
                cooldowns.put(player.getName(), System.currentTimeMillis());
            }else{
                cooldowns.put(player.getName(), System.currentTimeMillis());
                return;
            }

            ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
            ItemStack sheath = new ItemStack((Material.STICK));
            ItemStack sheathedSword = new ItemStack(Material.STICK);
            ItemStack air = new ItemStack(Material.AIR);

            ItemMeta sheathmeta = sheath.getItemMeta();
            sheathmeta.setCustomModelData(1000);
            ItemMeta swordmeta = sword.getItemMeta();
            swordmeta.setCustomModelData(1000);
            ItemMeta sheathedswordmeta = sheathedSword.getItemMeta();
            sheathedswordmeta.setCustomModelData(1001);
            sheath.setItemMeta(sheathmeta);
            sword.setItemMeta(swordmeta);
            sheathedSword.setItemMeta(sheathedswordmeta);

            player.getInventory().setItemInOffHand(sheath);
            player.getInventory().setItemInMainHand(sword);


            dashFwd(player);
            playDashSoundEffect(player);

            List<Entity> nearby = player.getNearbyEntities(2.5, 0, 2.5);
            for (Entity entity : nearby) {
                if (entity instanceof LivingEntity) {
                    LivingEntity le = (LivingEntity) entity;
                    le.damage(20);
                }
            }
            droppable=false;
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.instance, new Runnable()
            {
                @Override
                public void run()
                {
                    player.getInventory().setItemInOffHand(air);
                    player.getInventory().setItemInMainHand(sheathedSword);
                    if(player.getInventory().contains(Material.NETHERITE_SWORD))
                    {
                        player.getInventory().remove(Material.NETHERITE_SWORD);
                    }
                    droppable=true;
                }
            }, cooldownTime*20);
        }

    }

    public void dashFwd(Player player) {
        Location location = player.getLocation();
        Location startLocation = location;
        Vector direction = location.getDirection();
        direction.normalize();
        direction.multiply(5);
        location.add(direction);
        location.setY(Math.floor(location.getY()));

        Vector yVector = new Vector(0, 1, 0);
        Location test1 = location.add(yVector);
        Location test2 = test1.add(yVector);

        //World world = Bukkit.getServer().getWorld("world");
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
        world.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, (float) 0.7, (float) 0.7);

    }

    @EventHandler
    public void PreventNetheriteDrop(PlayerDropItemEvent event)
    {
        if(droppable==false)
        {
            if(event.getItemDrop().getItemStack().getType()==Material.NETHERITE_SWORD)
            {
                event.setCancelled(true);
            }
        }
        else
            return;
    }

    @EventHandler
    public void PreventNetheriteMove(InventoryClickEvent event)
    {
        if(droppable==false)
        {
            if(event.getCurrentItem().getType()==Material.NETHERITE_SWORD)
            {
                event.setCancelled(true);
            }
        }
        else
            return;
    }

    @EventHandler
    public void PreventNetheriteDrag(InventoryDragEvent dragEvent)
    {
        if(droppable==false)
        {
            if(dragEvent.getCursor().getType()==Material.NETHERITE_SWORD)
            {
                dragEvent.setCancelled(true);
            }
        }
        else
            return;
    }

}
