package me.hhh.amonplugin.listeners;

import me.hhh.amonplugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

public class Telettack implements Listener {

    private Main plugin;

    LivingEntity lockedTarget;

    public Telettack(Main plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void telettack(PlayerInteractEvent e)
    {
        if(plugin.telettack==false)
        {
            return;
        }
        Player player = e.getPlayer();
        Action action = e.getAction();


        if(!player.getName().equals("LowerCaseH"))
        {
            return;
        }

        if(action != Action.RIGHT_CLICK_AIR)
        {
            return;
        }

        if(lockedTarget==null)
        {
            return;
        }

        if(e.getItem().getType() != Material.DIAMOND_SWORD)
        {
            return;
        }

        player.sendMessage("Attack!");
        Location targetLoc = lockedTarget.getLocation();
        double x = 0;
        double z = 0;
        int range = 3;

        int i = ThreadLocalRandom.current().nextInt(1, 8+1);
        switch(i){
            case 1: x = targetLoc.getX()+range; z = targetLoc.getZ()+range;  break;
            case 2: x = targetLoc.getX()+range; z = targetLoc.getZ()-range;  break;
            case 3: x = targetLoc.getX()-range; z = targetLoc.getZ()+range;  break;
            case 4: x = targetLoc.getX()-range; z = targetLoc.getZ()-range;  break;
            case 5: x = targetLoc.getX()+range; z = targetLoc.getZ(); break;
            case 6: x = targetLoc.getX()-range; z = targetLoc.getZ(); break;
            case 7: x = targetLoc.getX(); z = targetLoc.getZ()+range; break;
            case 8: x = targetLoc.getX(); z = targetLoc.getZ()-range; break;
        }
        Location telLocation = new Location(Bukkit.getServer().getWorld("world"), x, targetLoc.getY(), z);
        float yaw = (float) Math.toDegrees(Math.atan2(telLocation.getZ() - targetLoc.getZ(), telLocation.getX() - targetLoc.getX())) - 90;
        telLocation.setYaw(yaw+180);
        player.sendMessage("Added effect to "+lockedTarget.getName());
        PotionEffect blind = new PotionEffect(PotionEffectType.BLINDNESS, 40, 3);
        blind.apply(lockedTarget);
        player.teleport(telLocation);
    }

    @EventHandler
    public void setLockedTarget(EntityDamageByEntityEvent e)
    {
        if(!(e.getDamager() instanceof Player))
        {
            return;
        }
        Player damager = (Player) e.getDamager();
        if((!damager.getName().equals("LowerCaseH")))
        {
            return;
        }
        if(!(e.getEntity() instanceof LivingEntity))
        {
            return;
        }
        if(e.getDamager() instanceof Player)
        {
            if(damager.getInventory().getItemInMainHand().getType().equals(Material.DIAMOND_SWORD))
            {
                damager.sendMessage("Locked onto "+e.getEntity().toString());
                lockedTarget = (LivingEntity) e.getEntity();
            }
        }
    }

}
