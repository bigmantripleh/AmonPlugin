package me.hhh.amonplugin.CustomMobs;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class PathfinderGoalDropItems extends PathfinderGoal {

    private final FoxCourierMob entity;
    private EntityHuman targetPlayer;

    private final double speed = 1.5;
    private final float maxDist;
    private final float minDist;
    private final NavigationAbstract navigation;
    private int timeOutTicks;

    public PathfinderGoalDropItems(FoxCourierMob entity, float maxDist, float minDist) {
        this.entity = entity;
        this.maxDist = maxDist;
        this.minDist = minDist;
        this.navigation = (NavigationAbstract) entity.getNavigation();
    }

    @Override
    public boolean a() {
        if (this.entity.getCooldown() > System.currentTimeMillis()) {
            return false;
        }

        List<EntityHuman> foundPlayers = entity.world.a(
                EntityHuman.class,
                entity.getBoundingBox().grow(maxDist, 4.0D, maxDist)
        );

        Set<UUID> droppedUUIDs = this.entity.getDroppedPlayers().stream()
                .map(Entity::getUniqueID)
                .collect(Collectors.toSet());

        List<EntityHuman> validPlayers = foundPlayers.stream()
                .filter(f -> !droppedUUIDs.contains(f.getUniqueID()))
                .collect(Collectors.toList());

        if (validPlayers.isEmpty()) {
            this.entity.getDroppedPlayers().clear();
            this.entity.setCooldown(System.currentTimeMillis() + 1000 * 30);
            return false;
        } else {
            this.targetPlayer = validPlayers.get(0);
            return true;
        }
    }

    @Override
    public void c() {
        this.timeOutTicks = 20 * 20;

        ItemStack itemstack = getItemStack(new Random().nextFloat());
        this.entity.setSlot(EnumItemSlot.MAINHAND, itemstack);

        this.navigation.a(targetPlayer, speed);
    }

    @Override
    public boolean b() {
        return targetPlayer != null &&
                targetPlayer.isAlive() &&
                entity.h(targetPlayer) > minDist * minDist &&
                timeOutTicks > 0;
    }

    @Override
    public void e() {
        if (targetPlayer != null) {
            this.entity.getControllerLook().a(targetPlayer, 30.0F, 30.0F);
            this.navigation.a(targetPlayer, speed);
            timeOutTicks--;
        }
    }

    @Override
    public void d() {
        if (targetPlayer != null) {
            EntityItem entityitem = new EntityItem(
                    this.entity.getWorld(),
                    this.entity.locX() + this.entity.getLookDirection().x,
                    this.entity.locY() + 1.0,
                    this.entity.locZ() + this.entity.getLookDirection().z,
                    this.entity.getItemInMainHand());
            entityitem.setPickupDelay(0);
            entityitem.setThrower(this.entity.getUniqueID());
            this.entity.playSound(SoundEffects.ENTITY_FOX_SPIT, 1.0F, 1.0F);
            this.entity.getWorld().addEntity(entityitem);

            this.entity.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.AIR));
            this.entity.getDroppedPlayers().add(this.targetPlayer);
        }

        this.targetPlayer = null;
        this.navigation.o();
    }

    private static ItemStack getItemStack(float f) {
        ItemStack itemstack;
        if (f < 0.025F) {
            itemstack = new ItemStack(Items.TOTEM_OF_UNDYING);
        } else if (f < 0.075F) {
            itemstack = new ItemStack(Items.ENCHANTED_GOLDEN_APPLE);
        } else if (f < 0.15F) {
            itemstack = new ItemStack(Items.cT);
            itemstack.addEnchantment(Enchantments.MENDING, 10);
            itemstack.a(new ChatComponentText(ChatColor.RED + "AIRSTRIKE"));
        } else if (f < 0.30F) {
            itemstack = new ItemStack(Items.GOLDEN_APPLE, 3);
        } else if (f < 0.50F) {
            itemstack = new ItemStack(Items.GOLDEN_CARROT, 32);
        } else {
            itemstack = new ItemStack(Items.COOKED_BEEF, 64);
        }
        return itemstack;
    }
}

