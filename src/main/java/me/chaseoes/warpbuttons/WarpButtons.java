package me.chaseoes.warpbuttons;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class WarpButtons extends JavaPlugin implements Listener {

	private static WarpButtons instance;

	public static WarpButtons getInstance() {
		return instance;
	}

	public void onEnable() {
		instance = this;
		getServer().getPluginManager().registerEvents(this, this);
	}

	public void onDisable() {
		reloadConfig();
		saveConfig();
		instance = null;
	}

	public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
		if (cmnd.getName().equalsIgnoreCase("warpbuttons")) {
			if (strings.length == 0) {
				cs.sendMessage(ChatColor.AQUA + "WarpButtons version " + getDescription().getVersion() + " by chaseoes.");
				return true;
			}

			if (strings[0].equalsIgnoreCase("create")) {
				if (cs instanceof Player) {
					if (strings.length == 2) {
						Warp warp = new Warp(strings[1]);
						warp.setLocation(((Player)cs).getLocation());
						cs.sendMessage(ChatColor.AQUA + "Successfully created the warp " + strings[1] + "!");
					} else {
						cs.sendMessage(ChatColor.RED + "Usage: /warpbuttons create <name>");
					}
				} else {
					cs.sendMessage("You must be a player to do that.");
				}
			}

			if (strings[0].equalsIgnoreCase("delete") || strings[0].equalsIgnoreCase("remove")) {
				if (strings.length == 2) {
					Warp warp = new Warp(strings[1]);
					warp.delete();
					cs.sendMessage(ChatColor.AQUA + "Successfully deleted the warp " + strings[1] + "!");
				} else {
					cs.sendMessage(ChatColor.RED + "Usage: /warpbuttons delete <name>");
				}
			}

			if (strings[0].equalsIgnoreCase("link")) {
				if (cs instanceof Player) {
					Player player = (Player) cs;
				if (strings.length == 2) {
					Warp warp = new Warp(strings[1]);
					if (warp.exists()) {
						warp.setLinkedButton(player.getTargetBlock(null, 5).getLocation());
						cs.sendMessage(ChatColor.AQUA + "Successfully linked to the warp " + strings[1] + "!");
					} else {
						cs.sendMessage(ChatColor.RED + "That warp does not exist!");
					}
				} else {
					cs.sendMessage(ChatColor.RED + "Usage: /warpbuttons link <name>");
				}
				} else {
					cs.sendMessage("You must be a player to do that.");
				}
			}
		}
		return true;
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if (event.hasBlock() && (event.getClickedBlock().getType() == Material.STONE_BUTTON || event.getClickedBlock().getType() == Material.WOOD_BUTTON)) {
			for (Warp w : getWarps()) {
				if (compareLocations(w.getLinkedButton(), event.getClickedBlock().getLocation())) {
					w.teleport(event.getPlayer());
				}
			}
		}
	}
	
	public List<Warp> getWarps() {
		List<Warp> warps = new ArrayList<Warp>();
		for (String s : getConfig().getConfigurationSection("warps").getKeys(false)) {
			warps.add(new Warp(s));
		}
		return warps;
	}
	
	public boolean compareLocations(Location l1, Location l2) {
		return (l1.getWorld().getName().equals(l2.getWorld().getName()) && l1.getBlockX() == l2.getBlockX() && l1.getBlockY() == l2.getBlockY() && l1.getBlockZ() == l2.getBlockZ());
	}

}
