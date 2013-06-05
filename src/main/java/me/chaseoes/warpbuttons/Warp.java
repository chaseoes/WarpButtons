package me.chaseoes.warpbuttons;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;

public class Warp {

    String name;
    List<Location> buttonCache = new ArrayList<Location>();
    FixedMetadataValue metadata;

    public Warp(String n) {
        name = n;
        metadata = new FixedMetadataValue(WarpButtons.getInstance(), this);
    }

    public String getName() {
        return name;
    }

    public boolean exists() {
        return getLocation() != null;
    }

    public boolean linkedButtonExists() {
        return getLinkedButtons().size() != 0;
    }

    public Location getLocation() {
        String s = WarpButtons.getInstance().getConfig().getString("warps." + getName() + ".location");
        if (s != null) {
            return loadLocation(s);
        }
        return null;
    }

    public List<Location> getLinkedButtons() {
        if (buttonCache == null) {
            List<String> s = WarpButtons.getInstance().getConfig().getStringList("warps." + getName() + ".linked-buttons");
            List<Location> locs = new ArrayList<Location>();
            if (s != null) {
                for (String str : s) {
                    locs.add(loadLocation(str));
                }
            }
            buttonCache = locs;
        }
        return buttonCache;
    }

    public void setLocation(Location l) {
        WarpButtons.getInstance().getConfig().set("warps." + getName() + ".location", saveLocation(l));
        WarpButtons.getInstance().saveConfig();
    }

    public boolean addLinkedButton(Location l) {
        List<String> locs = WarpButtons.getInstance().getConfig().getStringList("warps." + getName() + ".linked-buttons");
        l = normalize(l);
        boolean flag = true;
        for (int i = 0; i < locs.size(); i++) {
            if (compareLocations(loadLocation(locs.get(i)), l)) {
                flag = false;
            }
        }
        if (flag) {
            locs.add(saveLocation(l));
            WarpButtons.getInstance().getConfig().set("warps." + getName() + ".linked-buttons", locs);
            WarpButtons.getInstance().saveConfig();
            l.getBlock().setMetadata("warpbuttons", metadata);
        }
        return flag;
    }

    public boolean removeLinkedButton(Location l) {
        List<String> locs = WarpButtons.getInstance().getConfig().getStringList("warps." + getName() + ".linked-buttons");
        l = normalize(l);
        boolean flag = false;
        for (int i = 0; i < locs.size(); i++) {
            if (compareLocations(loadLocation(locs.get(i)), l)) {
                flag = true;
                locs.remove(i);
            }
        }
        if (flag) {
            WarpButtons.getInstance().getConfig().set("warps." + getName() + ".linked-buttons", locs);
            WarpButtons.getInstance().saveConfig();
            l.getBlock().removeMetadata("warpbuttons", WarpButtons.getInstance());
        }
        return flag;
    }

    public void teleport(Player player) {
        Location loc = getLocation();
        if (loc != null) {
            player.teleport(getLocation().add(0.5, 0, 0.5));
        }
    }

    private Location loadLocation(String s) {
        String[] sp = s.split(",");
        return new Location(WarpButtons.getInstance().getServer().getWorld(sp[0]), Integer.parseInt(sp[1]), Integer.parseInt(sp[2]), Integer.parseInt(sp[3]));
    }

    private String saveLocation(Location l) {
        return l.getWorld().getName() + "," + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
    }

    private Location normalize(Location l) {
        return new Location(l.getWorld(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
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

    public void invalidateCache() {
        buttonCache = null;
    }

    public void initMetadata() {
        for (Location loc : getLinkedButtons()) {
            loc.getBlock().setMetadata("warpbuttons", metadata);
        }
    }

    public boolean compareLocations(Location l1, Location l2) {
        return (l1.getWorld().getName().equals(l2.getWorld().getName()) && l1.getBlockX() == l2.getBlockX() && l1.getBlockY() == l2.getBlockY() && l1.getBlockZ() == l2.getBlockZ());
    }
}
