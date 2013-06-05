package me.chaseoes.warpbuttons;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

public class WarpButtons extends JavaPlugin implements Listener {

    private static WarpButtons instance;
    private List<Warp> warps = new ArrayList<Warp>();

    public static WarpButtons getInstance() {
        return instance;
    }

    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(this, this);
        if (getConfig().isConfigurationSection("warps")) {
            for (String s : getConfig().getConfigurationSection("warps").getKeys(false)) {
                Warp warp = new Warp(s);
                warp.initMetadata();
                warps.add(warp);
            }
        }
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
            if (cs.hasPermission("warpbuttons.admin")) {
                if (strings[0].equalsIgnoreCase("create")) {
                    if (cs instanceof Player) {
                        if (strings.length == 2) {
                            Warp warp = new Warp(strings[1]);
                            warp.setLocation(((Player) cs).getLocation());
                            cs.sendMessage(ChatColor.AQUA + "Successfully created the warp " + strings[1] + "!");
                        } else {
                            cs.sendMessage(ChatColor.RED + "Usage: /warpbuttons create <name>");
                        }
                    } else {
                        cs.sendMessage("You must be a player to do that.");
                    }
                } else if (strings[0].equalsIgnoreCase("delete") || strings[0].equalsIgnoreCase("remove")) {
                    if (strings.length == 2) {
                        Warp warp = new Warp(strings[1]);
                        warp.delete();
                        cs.sendMessage(ChatColor.AQUA + "Successfully deleted the warp " + strings[1] + "!");
                    } else {
                        cs.sendMessage(ChatColor.RED + "Usage: /warpbuttons delete <name>");
                    }
                } else if (strings[0].equalsIgnoreCase("link")) {
                    if (cs instanceof Player) {
                        Player player = (Player) cs;
                        if (strings.length > 1) {
                            if (strings[1].equalsIgnoreCase("add")) {
                                Warp warp = new Warp(strings[2]);
                                if (warp.exists()) {
                                    if (warp.addLinkedButton(player.getTargetBlock(null, 5).getLocation())) {
                                        cs.sendMessage(ChatColor.AQUA + "Successfully linked to the warp " + strings[1] + "!");
                                    } else {
                                        cs.sendMessage(ChatColor.RED + "Button is already linked");
                                    }
                                } else {
                                    cs.sendMessage(ChatColor.RED + "That warp does not exist!");
                                }
                            } else if (strings[1].equalsIgnoreCase("remove")) {
                                Block b = player.getTargetBlock(null, 5);
                                if (b == null) {
                                    cs.sendMessage(ChatColor.RED + "Button not targeted");
                                } else {
                                    if (b.hasMetadata("warpbuttons")) {
                                        Warp warp = (Warp) b.getMetadata("warpbuttons").get(0).value();
                                        if (warp.removeLinkedButton(b.getLocation())) {
                                            cs.sendMessage(ChatColor.AQUA + "Unlinked button");
                                        } else {
                                            cs.sendMessage(ChatColor.RED + "Could not unlink button");
                                        }
                                    } else {
                                        cs.sendMessage(ChatColor.RED + "Button is not linked");
                                    }
                                }
                            } else {
                                cs.sendMessage(ChatColor.RED + "Usage: /warpbuttons link [add|remove] [name]");
                            }

                        } else {
                            cs.sendMessage(ChatColor.RED + "Usage: /warpbuttons link [add|remove] [name]");
                        }
                    } else {
                        cs.sendMessage("You must be a player to do that.");
                    }
                } else {
                    cs.sendMessage(ChatColor.RED + "Usage: /warpbuttons link [add|remove] [name]");
                }
            } else {
                cs.sendMessage(ChatColor.RED + "You don't have permission to do that");
            }
        }
        return true;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.hasBlock() && (event.getClickedBlock().getType() == Material.STONE_BUTTON || event.getClickedBlock().getType() == Material.WOOD_BUTTON) && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getClickedBlock().hasMetadata("warpbuttons")) {
                List<MetadataValue> vals = event.getClickedBlock().getMetadata("warpbuttons");
                FixedMetadataValue val = (FixedMetadataValue) vals.get(0);
                Warp warp = (Warp) val.value();
                warp.teleport(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.STONE_BUTTON || event.getBlock().getType() == Material.WOOD_BUTTON) {
            if (event.getBlock().hasMetadata("warpbuttons")) {
                if (!event.getPlayer().hasPermission("warpbuttons.admin")) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED + "You can't break a Warp Button");
                } else {
                    Warp warp = (Warp) event.getBlock().getMetadata("warpbuttons").get(0).value();
                    if (warp.removeLinkedButton(event.getBlock().getLocation())) {
                        event.getPlayer().sendMessage(ChatColor.AQUA + "Unlinked button");
                    } else {
                        event.getPlayer().sendMessage(ChatColor.RED + "Could not unlink button");
                    }
                }
            }
        }
    }

    public List<Warp> getWarps() {
        return warps;
    }

    public boolean compareLocations(Location l1, Location l2) {
        return (l1.getWorld().getName().equals(l2.getWorld().getName()) && l1.getBlockX() == l2.getBlockX() && l1.getBlockY() == l2.getBlockY() && l1.getBlockZ() == l2.getBlockZ());
    }

}
