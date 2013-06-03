package me.chaseoes.warpbuttons;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Warp {
	
	String name;
	
	public Warp(String n) {
		name = n;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean exists() {
		return getLocation() != null;
	}
	
	public boolean linkedButtonExists() {
		return getLinkedButton() != null;
	}
	
	public Location getLocation() {
		String s = WarpButtons.getInstance().getConfig().getString("warps." + getName() + ".location");
		if (s != null) {
			return loadLocation(s);
		}
		return null;
	}
	
	public Location getLinkedButton() {
		String s = WarpButtons.getInstance().getConfig().getString("warps." + getName() + ".linked-button");
		if (s != null) {
			return loadLocation(s);
		}
		return null;
	}
	
	public void setLocation(Location l) {
		WarpButtons.getInstance().getConfig().set("warps." + getName() + ".location", l.getWorld().getName() + "," + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ());
		WarpButtons.getInstance().saveConfig();
	}
	
	public void setLinkedButton(Location l) {
		WarpButtons.getInstance().getConfig().set("warps." + getName() + ".linked-button", l.getWorld().getName() + "," + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ());
		WarpButtons.getInstance().saveConfig();
	}
	
	public void teleport(Player player) {
		player.teleport(getLocation().add(0.5, 0, 0.5));
	}
	
	private Location loadLocation(String s) {
		String[] sp = s.split(",");
		return new Location(WarpButtons.getInstance().getServer().getWorld(sp[0]), Integer.parseInt(sp[1]), Integer.parseInt(sp[2]), Integer.parseInt(sp[3]));
	}
	
	public void delete() {
		if (exists()) {
			WarpButtons.getInstance().getConfig().set("warps." + getName() + ".location", null);
			if (linkedButtonExists()) {
				WarpButtons.getInstance().getConfig().set("warps." + getName() + ".linked-button", null);
			}
			WarpButtons.getInstance().saveConfig();
		}
	}

}
