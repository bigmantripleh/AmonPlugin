package me.hhh.amonplugin.CustomMobs;

import net.minecraft.server.v1_16_R3.*;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class FoxCourierMob extends EntityFox {

    public static final NamespacedKey key = new NamespacedKey("amonplugin", "foxcouriermob");
    private final Player player;
    private Long cooldown;
    private List<EntityHuman> droppedPlayers = new ArrayList<>();

    public FoxCourierMob(Location loc, Player player) {

        super(EntityTypes.FOX, ((CraftWorld) loc.getWorld()).getHandle());
        this.player = player;
        this.setPosition(loc.getX(), loc.getY(), loc.getZ());
        this.setCanPickupLoot(false);
        this.getBukkitEntity().getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        this.getDataWatcher().set(EntityFox.FIRST_TRUSTED_PLAYER, Optional.of(player.getUniqueId()));
        this.persist = true;
        this.cooldown = 0L;
        ((CraftWorld) loc.getWorld()).getHandle().addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
        this.goalSelector.a(1, new PathfinderGoalFollowPlayer(this, player));

    }

    @Override
    protected void initPathfinder() {
        this.goalSelector = new PathfinderGoalSelector(world.getMethodProfilerSupplier());
        this.targetSelector = new PathfinderGoalSelector(world.getMethodProfilerSupplier());
        this.goalSelector.a(3, new PathfinderGoalRandomLookaround(this));
        this.goalSelector.a(2, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 10.0F));
        this.goalSelector.a(0, new PathfinderGoalDropItems(this, 30, 2));
    }

    public void setCooldown(Long cooldown) {
        this.cooldown = cooldown;
    }
    public Long getCooldown() {
        return this.cooldown;
    }

    public List<EntityHuman> getDroppedPlayers() {
        return this.droppedPlayers;
    }

    public void setDroppedPlayers(List<EntityHuman> droppedPlayers) {
        this.droppedPlayers = droppedPlayers;
    }
}
