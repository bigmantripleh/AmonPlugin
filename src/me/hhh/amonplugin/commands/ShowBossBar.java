package me.hhh.amonplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ShowBossBar implements CommandExecutor {

    private BossBar bossBar;
    private JavaPlugin plugin;

    public ShowBossBar(JavaPlugin plugin) {
        this.plugin = plugin;
        createBossBar();
    }

    private void createBossBar() {
        bossBar = Bukkit.createBossBar("LowerCaseH HP", BarColor.RED, BarStyle.SOLID);
        bossBar.setVisible(false); // Initially hidden
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("showhbar")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be executed by a player.");
                return true;
            }

            if (args.length > 0 && args[0].equalsIgnoreCase("hide")) {
                hideBossBar();
                sender.sendMessage("Boss bar has been hidden from all players.");
                return true;
            }

            Player target = Bukkit.getPlayerExact("LowerCaseH");
            if (target == null) {
                sender.sendMessage("The player LowerCaseH is not online.");
                return true;
            }

            bossBar.setVisible(true);

            // Add all online players to the boss bar
            for (Player player : Bukkit.getOnlinePlayers()) {
                bossBar.addPlayer(player);
            }

            sender.sendMessage("Boss bar showing LowerCaseH's HP is now visible to all players.");

            // Start a repeating task to update the boss bar every 2 ticks (0.1 seconds)
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (bossBar.isVisible() && target.isOnline()) {
                        updateBossBar(target);
                    } else {
                        bossBar.setVisible(false);
                        this.cancel(); // Stop the task if the boss bar is no longer visible or the player is offline
                    }
                }
            }.runTaskTimer(plugin, 0L, 2L);

            return true;
        }
        return false;
    }

    private void updateBossBar(Player target) {
        double health = target.getHealth();
        double maxHealth = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

        bossBar.setProgress(health / maxHealth);
        bossBar.setTitle("LowerCaseH HP: " + String.format("%.1f", health) + "/" + String.format("%.1f", maxHealth));
    }

    private void hideBossBar() {
        bossBar.removeAll();  // Removes all players from the boss bar
        bossBar.setVisible(false);  // Hides the boss bar
    }
}
