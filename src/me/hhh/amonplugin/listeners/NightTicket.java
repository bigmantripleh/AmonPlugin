package me.hhh.amonplugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class NightTicket implements Listener {
    @EventHandler
    public void DayTicket(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        Action action = event.getAction();

        if(!action.equals(Action.RIGHT_CLICK_AIR)&&!action.equals(Action.RIGHT_CLICK_BLOCK))
        {
            return;
        }
        if(!event.hasItem())
        {
            return;
        }
        if(event.getItem().getType() != Material.PAPER)
        {
            return;
        }

        if(event.getItem().getItemMeta().hasEnchant(Enchantment.IMPALING))
        {
            if(event.getItem().getAmount()==0)
            {
                player.getInventory().setItemInMainHand(null);
            }
            else{
                event.getItem().setAmount(event.getItem().getAmount()-1);
            }
            Bukkit.getServer().getWorld("world").setTime(14500);
            Bukkit.broadcastMessage("\n"+ChatColor.BLUE+event.getPlayer().getName()+" has used a"+ChatColor.RED+" Night Ticket"+ChatColor.BLUE+" to make it night. Beware of the monsters!\n ");
        }
        return;
    }
}