package me.hhh.amonplugin.CustomMobs;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PathfinderGoalFollowPlayer extends PathfinderGoal {

    private FoxCourierMob fox;
    private EntityPlayer targetPlayer;

    private final double speed = 1;
    private final float maxDist;
    private final float minDist;
    private final NavigationAbstract navigation;

    public PathfinderGoalFollowPlayer(FoxCourierMob fox, Player player) {
        this.fox = fox;
        this.maxDist = 20;
        this.minDist = 2;
        this.navigation = (NavigationAbstract) fox.getNavigation();

        Player bukkitPlayer = player;
        CraftPlayer craftPlayer = (CraftPlayer) bukkitPlayer;
        targetPlayer = craftPlayer.getHandle();

    }


    @Override
    public boolean a() { // shouldExecute
        System.out.println(targetPlayer);
        if(this.fox.getCooldown() < System.currentTimeMillis()){
            return false;
        }
        System.out.println(this.fox.getCooldown() - System.currentTimeMillis());
        if (targetPlayer == null || !targetPlayer.isAlive()) {
            return false;
        }

        return !(fox.h(targetPlayer) < (double) (minDist * minDist));
    }

    @Override
    public boolean b() { // shouldContinue
        if(this.fox.getCooldown() < System.currentTimeMillis()){
            return false;
        }
        return !navigation.m() && fox.h(targetPlayer) > (double)(maxDist * maxDist);
    }

    @Override
    public void c() { // start
        this.navigation.a(targetPlayer, speed);
    }

    @Override
    public void d() { // stop
        this.navigation.o();
    }

    @Override
    public void e() { // tick
        if (targetPlayer != null) {
            this.fox.getControllerLook().a(targetPlayer, 30.0F, 30.0F);
            this.navigation.a(targetPlayer, speed);
        }
    }
}