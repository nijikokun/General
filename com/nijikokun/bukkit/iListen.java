package com.nijikokun.bukkit;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.bukkit.Player;
import org.bukkit.World;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.Plugin;
import net.minecraft.server.WorldServer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;


/**
 * General 1.1 & Code from iConomy 2.x
 * Copyright (C) 2011  Nijikokun <nijikokun@gmail.com>
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

    /**
     * AFK
     */
    public ArrayList<Player> AFK = new ArrayList<Player>();

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
	Messaging.send("&f /me - Emote your messages                           ");
	Messaging.send("&f /afk - Go away or come back                         ");
	Messaging.send("&f /message|tell|m [player] [message] - Private msg    ");
	Messaging.send("&f /compass|getpos - information about position        ");
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

    public static String getDirection(double degrees) {
        if (0 <= degrees && degrees < 22.5) {
            return "N";
        } else if (22.5 <= degrees && degrees < 67.5) {
            return "NE";
        } else if (67.5 <= degrees && degrees < 112.5) {
            return "E";
        } else if (112.5 <= degrees && degrees < 157.5) {
            return "SE";
        } else if (157.5 <= degrees && degrees < 202.5) {
            return "S";
        } else if (202.5 <= degrees && degrees < 247.5) {
            return "SW";
        } else if (247.5 <= degrees && degrees < 292.5) {
            return "W";
        } else if (292.5 <= degrees && degrees < 337.5) {
            return "NW";
        } else if (337.5 <= degrees && degrees < 360.0) {
            return "N";
        } else {
            return "ERR";
        }
    }

    public boolean isAFK(Player player) {
	return AFK.contains(player);
    }

    public void AFK(Player player) {
	AFK.add(player);
    }

    public void unAFK(Player player) {
	AFK.remove(player);
    }

    public String[] readMotd() {
	ArrayList<String> lines = new ArrayList<String>();

	try {
	    BufferedReader in = new BufferedReader(new FileReader(General.directory + "general.motd"));
	    String str;
	    while ((str = in.readLine()) != null) {
		lines.add(str);
	    }
	    in.close();
	} catch (IOException e) { }

	return lines.toArray(new String[]{});
    }

    @Override
    public void onPlayerJoin(PlayerEvent event) {
        Player player = event.getPlayer();
	String[] motd = readMotd();

	if(motd == null || motd.length < 1) { return; }

	for(String line : motd) {
	    Messaging.send(player, line);
	}
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

	if(Misc.is(base, "/motd")) {
	    String[] motd = readMotd();

	    if(motd == null || motd.length < 1) { return; }

	    for(String line : motd) {
		Messaging.send(player, line);
	    }
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

	if(Misc.isEither(base, "/s", "/tphere")) {
	    if (!General.Watch.permission("teleport", player)) {
		return;
	    }

	    if (split.length < 1) {
		Messaging.send("&cCorrect usage is:&f /s [player] &cor&f /tphere [player]");
		return;
	    }

	    Player who = Misc.playerMatch(split[1]);

	    if (player != null) {
		if (who.getName().equalsIgnoreCase(player.getName())) {
		    Messaging.send("&cWow look at that! You teleported yourself to yourself!");
		    return;
		}

		log.info(player.getName() + " teleported " + who.getName() + " to their self.");
		who.teleportTo(player.getLocation());
	    } else {
		Messaging.send("&cCan't find user " + split[1] + ".");
	    }

	    event.setCancelled(true);
	}

	if(Misc.is(base, "/getpos")) {
	    Messaging.send("Pos X: " + player.getLocation().getX() + " Y: " + player.getLocation().getY() + " Z: " + player.getLocation().getZ());
	    Messaging.send("Rotation: " + player.getLocation().getYaw() + " Pitch: " + player.getLocation().getPitch());

	    double degreeRotation = ((player.getLocation().getYaw() - 90) % 360);

	    if (degreeRotation < 0) {
		degreeRotation += 360.0;
	    }

	    Messaging.send("Compass: " + getDirection(degreeRotation) + " (" + (Math.round(degreeRotation * 10) / 10.0) + ")");

	    event.setCancelled(true);
	}
	
	if(Misc.is(base, "/compass")) {
	    double degreeRotation = ((player.getLocation().getYaw() - 90) % 360);

	    if (degreeRotation < 0) {
		degreeRotation += 360.0;
	    }

	    Messaging.send("&cCompass: " + getDirection(degreeRotation));
	    
	    event.setCancelled(true);
	}

	if(Misc.isEither(base, "/afk", "/away")) {
	    if ((AFK != null || !AFK.isEmpty()) && isAFK(player)) {
		Messaging.send("&7You have been marked as back.");
		unAFK(player);
	    } else {
		Messaging.send("&7You are now currently marked as away.");
		AFK(player);
	    }

	    event.setCancelled(true);
	}

	if(Misc.isEither(base, "/msg", "/tell")) {
	    if (split.length < 3) {
		Messaging.send("&cCorrect usage is: /msg [player] [message]");
		return;
	    }

	    if (isAFK(player)) {
		Messaging.send("&7This player is currently away.");
	    }

	    Player who = Misc.playerMatch(split[1]);

	    if (who != null) {
		if (who.getName().equals(player.getName())) {
		    Messaging.send("&cYou can't message yourself!");
		    return;
		}

		Messaging.send("(MSG) <" + player.getName() + "> " + Misc.combineSplit(2, split, " "));
		Messaging.send(who, "(MSG) <" + player.getName() + "> " + Misc.combineSplit(2, split, " "));
	    } else {
		Messaging.send("&cCouldn't find player " + split[1]);
	    }
	}

	if(Misc.isEither(base, "/afk", "/away")) {
	    if (isAFK(player)) {
		Messaging.send("&7You have been marked as back.");
		unAFK(player);
	    } else {
		Messaging.send("&7You are now currently marked as away.");
		AFK(player);
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
		Messaging.send("&ePlayers &fcurrently&e online:");
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
	    Messaging.send("&6    -&e Status: &f" + ((isAFK(current)) ? "afk" : "around"));
	    Messaging.send("&f------------------------------------------------"    );
	    event.setCancelled(true);
	}
    }
}
