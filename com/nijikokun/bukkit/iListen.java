package com.nijikokun.bukkit;


import java.util.logging.Logger;
import org.bukkit.Player;
import org.bukkit.World;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.Plugin;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldServer;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;


/**
 * iConomy v1.x
 * Copyright (C) 2010  Nijikokun <nijikokun@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * iListen.java
 * <br /><br />
 * Listens for calls from hMod, and reacts accordingly.
 * 
 * @author Nijikokun <nijikokun@gmail.com>
 */
public class iListen extends PlayerListener {

    private static final Logger log = Logger.getLogger("Minecraft");

    /**
     * Miscellaneous object for various functions that don't belong anywhere else
     */
    public Misc Misc = new Misc();

    public static General plugin;
    public General p;
    public WorldServer server;

    public iListen(General instance) {
        plugin = instance;
        p = instance;
    }

    /**
     * Sends simple condensed help lines to the current player
     */
    private void showSimpleHelp() {
	Messaging.send("&e-----------------------------------------------------");
	Messaging.send("&f General (&c"+General.codename+"&f)                  ");
	Messaging.send("&e-----------------------------------------------------");
	Messaging.send("&f [] Required, () Optional                            ");
	Messaging.send("&e-----------------------------------------------------");
	Messaging.send("&f /playerlist or /online &6- &elists players online.  ");
	Messaging.send("&f /enable|disable|reload [plugin] &6- &eManage plugins");
	Messaging.send("&f /who (player) &6- &ePlayer information              ");
	Messaging.send("&f /spawn &6- &eReturn to spawn                        ");
	Messaging.send("&f /setspawn &6- &eChange spawn to where you are       ");
	Messaging.send("&f /time (day|night|raw) &6- &eChange the time         ");
	Messaging.send("&f /help or /? &6- &eReturns this documentation        ");
	Messaging.send("&e-----------------------------------------------------");
    }

    private Location spawn(Player player) {
	double x = (server.m + 0.5D);
	double y = server.e(this.server.m, this.server.o) + 1.5D;
	double z = server.o + 0.5D;
	float rotX = 0.0F;
	float rotY = 0.0F;

	return new Location(player.getWorld(), x, y, z, rotX, rotY);
    }

    /**
     * Returns actual server time (-2^63 to 2^63-1)
     *
     * @return time server time
     */
    public long getTime() {
        return server.e;
    }

    /**
     * Returns current server time (0-24000)
     *
     * @return time server time
     */
    public long getRelativeTime() {
        long time = (server.e % 24000);
        // Java modulus is stupid.
        if (time < 0) {
            time += 24000;
        }
        return time;
    }

    /**
     * Sets the actual server time
     *
     * @param time
     *            time (-2^63 to 2^63-1)
     */
    public void setTime(long time) {
        server.e = time;
    }

    /**
     * Sets the current server time
     *
     * @param time
     *            time (0-24000)
     */
    public void setRelativeTime(long time) {
        long margin = (time - server.e) % 24000;
        // Java modulus is stupid.
        if (margin < 0) {
            margin += 24000;
        }
        server.e += margin;
    }

    /**
     * Commands sent from in game to us.
     *
     * @param player The player who sent the command.
     * @param split The input line split by spaces.
     * @return <code>boolean</code> - True denotes that the command existed, false the command doesn't.
     */
    @Override
    public void onPlayerCommand(PlayerChatEvent event) {
        String[] split = event.getMessage().split(" ");
        Player player = event.getPlayer();
	World world = player.getWorld();
	server = ((CraftWorld)world).getHandle();
	Messaging.save(player);
	String base = split[0];
	
	if(Misc.isEither(base, "/help", "/?")) {
	    showSimpleHelp();
	    event.setCancelled(true);
	}
	
	if(Misc.is(base, "/setspawn")) {
	    if (!General.Watch.permission("set-spawn", player)) {
		return;
	    }
	    
	    server.m = (int)Math.ceil(player.getLocation().getX());
	    server.o = (int)Math.ceil(player.getLocation().getZ());
	    
	    Messaging.send("&eSpawn position changed to where you are standing.");
	    event.setCancelled(true);
	}

	if(Misc.is(base, "/spawn")) {
	    if (!General.Watch.permission("spawn", player)) {
		return;
	    }

	    player.teleportTo(spawn(player));
	    event.setCancelled(true);
	}

	if(Misc.isEither(base, "/tp", "/teleport")) {
	    if (!General.Watch.permission("teleport", player)) {
		return;
	    }

	    if (split.length < 1) {
		Messaging.send("&cCorrect usage is: /tp [player]");
		return;
	    }

	    Player who = Misc.playerMatch(split[1]);

	    if (player != null) {
		if (player.getName().equalsIgnoreCase(who.getName())) {
		    Messaging.send("&cCannot teleport to self! It's against time law yanno.");
		    return;
		}

		log.info(player.getName() + " teleported to " + who.getName());
		player.teleportTo(who.getLocation());
	    } else {
		Messaging.send("&cCan't find user " + split[1] + ".");
	    }

	    event.setCancelled(true);
	}

	if(Misc.is(base, "/time")) {
	    if (!General.Watch.permission("set-time", player)) {
		return;
	    }

	    if (split.length == 2) {
		if (split[1].equalsIgnoreCase("day")) {
		    setRelativeTime(0);
		} else if (split[1].equalsIgnoreCase("night")) {
		    setRelativeTime(13000);
		} else if (split[1].equalsIgnoreCase("check")) {
		    Messaging.send("&cThe time is " + getRelativeTime() + "! (RAW: " + getTime() + ")");
		} else {
		    try {
			setRelativeTime(Long.parseLong(split[1]));
		    } catch (NumberFormatException ex) {
			Messaging.send("&cPlease enter numbers, not letters.");
		    }
		}
	    } else if (split.length == 3) {
		if (split[1].equalsIgnoreCase("raw")) {
		    try {
			setTime(Long.parseLong(split[2]));
		    } catch (NumberFormatException ex) {
			Messaging.send("&cPlease enter numbers, not letters.");
		    }
		}
	    } else {
		Messaging.send("&cCorrect usage is: /time [time|'day|night|check|raw'] (rawtime)");
		return;
	    }
	}

	if(Misc.isEither(base, "/playerlist", "/online")) {
	    if(p.getServer().getOnlinePlayers() == null) {
		Messaging.send(" ");
		Messaging.send("&e Players &fcurrently&e online:");
		Messaging.send("&f - Just you.");
		Messaging.send(" ");
		event.setCancelled(true);
		return;
	    }

	    Player[] online = p.getServer().getOnlinePlayers();

	    String list = "";
	    int length = (online.length-1);
	    int on = 0;

	    for(Player current : online) {
		if(current == null) { ++on; continue; }

		list += (on >= length) ? current.getName() : current.getName() + ", ";
		++on;
	    }

	    Messaging.send(" ");
	    Messaging.send("&e Players &fcurrently&e online:");
	    Messaging.send(list);
	    Messaging.send(" ");

	    event.setCancelled(true);
	}

	if(Misc.isEither(base, "/reload", "/reloadplugin")) {
	    if (!General.Watch.permission("manage-plugins", player)) {
		return;
	    }

	    if(split.length < 2) {
		Messaging.send("&cInvalid parameters.");
		Messaging.send("&cUsage:&f /reload [plugin]");
		event.setCancelled(true);
		return;
	    }

	    String plugin = split[1];
	    boolean enabled = p.getServer().getPluginManager().isPluginEnabled(plugin);
	    Plugin reloading = p.getServer().getPluginManager().getPlugin(plugin);

	    if(enabled) {
		p.getPluginLoader().disablePlugin(reloading);
		Messaging.send("&ePlugin [&f"+plugin+"&e] Disabled.");
		event.setCancelled(true);
		return;
	    }

	    p.getPluginLoader().enablePlugin(reloading);
	    Messaging.send("&ePlugin [&f"+plugin+"&e] Enabled.");

	    event.setCancelled(true);
	}

	if(Misc.isEither(base, "/enable", "/enableplugin")) {
	    if (!General.Watch.permission("manage-plugins", player)) {
		return;
	    }

	    if(split.length < 2) {
		Messaging.send("&cInvalid parameters.");
		Messaging.send("&cUsage:&f /enable [plugin]");
		event.setCancelled(true);
		return;
	    }

	    String plugin = split[1];
	    boolean enabled = p.getServer().getPluginManager().isPluginEnabled(plugin);
	    Plugin reloading = p.getServer().getPluginManager().getPlugin(plugin);

	    if(!enabled) {
		p.getPluginLoader().enablePlugin(reloading);
		Messaging.send("&ePlugin [&f"+plugin+"&e] Enabled.");
		event.setCancelled(true);
		return;
	    }

	    Messaging.send("&ePlugin [&f"+plugin+"&e] is already enabled.");
	    event.setCancelled(true);
	}

	if(Misc.isEither(base, "/disable", "/disableplugin")) {
	    if (!General.Watch.permission("manage-plugins", player)) {
		return;
	    }

	    if(split.length < 2) {
		Messaging.send("&cInvalid parameters.");
		Messaging.send("&cUsage:&f /disable [plugin]");
		event.setCancelled(true);
		return;
	    }

	    String plugin = split[1];
	    boolean enabled = p.getServer().getPluginManager().isPluginEnabled(plugin);

	    if(enabled) {
		Plugin reloading = p.getServer().getPluginManager().getPlugin(plugin);

		p.getPluginLoader().disablePlugin(reloading);
		Messaging.send("&ePlugin [&f"+plugin+"&e] Disabled.");
		event.setCancelled(true);
		return;
	    }

	    Messaging.send("&ePlugin [&f"+plugin+"&e] is not enabled.");
	    event.setCancelled(true);
	}

	if(Misc.isEither(base, "/who", "/info")) {
	    String looking = "";
	    Player current = null;

	    if(split.length < 2) {
		looking = player.getName();
	    } else {
		looking = split[1];
	    }

	    if(looking.equals(player.getName())) {
		current = player;
	    } else {
		current = plugin.getServer().getPlayer(looking);
	    }

	    if(current == null) {
		Messaging.send("&cThat player is either not online, or does not exist!");
		event.setCancelled(true);
		return;
	    }

	    String name = current.getName();
	    String hb_color = "&2";
	    int health = current.getHealth();
	    int x = (int)current.getLocation().getX();
	    int y = (int)current.getLocation().getY();
	    int z = (int)current.getLocation().getZ();

	    int length = 10;
	    int bars = Math.round(health/2);
	    int remainder = length-bars;

	    if(bars >= 7) {
		hb_color = "&2";
	    } else if(bars < 7 && bars >= 3) {
		hb_color = "&e";
	    } else if(bars < 3) {
		hb_color = "&c";
	    }

	    String bar = hb_color + Misc.repeat('|', bars) + "&4" + Misc.repeat('|', remainder);
	    String location = x+"x, "+y+"y, "+z+"z";

	    Messaging.send("&f------------------------------------------------"    );
	    Messaging.send("&e Information for &f"+current.getName()+"&e:"         );
	    Messaging.send("&f------------------------------------------------"    );
	    Messaging.send("&6  Username: &f" + current.getName() + " ["+bar+"&f]" );
	    Messaging.send("&6    -&e Location: &f" + location                     );
	    Messaging.send("&f------------------------------------------------"    );
	    event.setCancelled(true);
	}
    }
}
