package me.hhh.amonplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SetCustomModelData implements CommandExecutor
{

  @Override
  public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings)
  {
    if(!(commandSender instanceof Player))
    {
      return false;
    }
    if(strings[0]==null)
    {
      return false;
    }
    if(strings.length>1)
    {
      return false;
    }
    int data = Integer.parseInt(strings [0]);
    Player player = (Player) commandSender;
    ItemStack item = player.getInventory().getItemInMainHand();
    ItemMeta meta = item.getItemMeta();
    meta.setCustomModelData(data);
    item.setItemMeta(meta);
    player.getInventory().setItemInMainHand(item);
    return true;
  }
}
