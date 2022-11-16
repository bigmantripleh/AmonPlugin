package me.hhh.amonplugin;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class DrawCut implements Listener
{
  @EventHandler
  public void onRightClick(PlayerInteractEvent e)
  {
    Player player = e.getPlayer();
    Action a = e.getAction();

  if(a != Action.RIGHT_CLICK_AIR)
  {
    return;
  }

  if(e.getItem().getType() == Material.BEETROOT)
  {
    player.sendMessage("Na du nigger");
  }
  }
}
