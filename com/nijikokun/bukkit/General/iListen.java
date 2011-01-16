package com.nijikokun.bukkit.General;

import com.nijikokun.bukkit.iConomy.iConomy;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.World;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.Plugin;
import net.minecraft.server.WorldServer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


/**
 * General 1.1 & Code from iConomy 2.x
 * Coded while listening to Avenged Sevenfold - A little piece of heaven <3
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
    public HashMap<Player, String> AFK = new HashMap<Player, String>();

    /**
     * Commands
     */
    public List<String> Commands = new ArrayList<String>();

    public static General plugin;
    public WorldServer server;

    public iListen(General instance) {
        plugin = instance;
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
        return plugin.getServer().getTime();
    }

    /**
     * Returns current server time (0-24000)
     *
     * @return time server time
     */
    public long getRelativeTime() {
        long time = (getTime() % 24000);
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
        plugin.getServer().setTime(time);
    }

    /**
     * Sets the current server time
     *
     * @param time
     *            time (0-24000)
     */
    public void setRelativeTime(long time) {
        long margin = (time-getTime()) % 24000;
        // Java modulus is stupid.
        if (margin < 0) {
            margin += 24000;
        }

        plugin.getServer().setTime(getTime()+margin);
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
	return AFK.containsKey(player);
    }

    public void AFK(Player player, String message) {
	AFK.put(player, message);
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

    public String[] read_commands() {
	ArrayList<String> lines = new ArrayList<String>();

	try {
	    BufferedReader in = new BufferedReader(new FileReader(General.directory + "general.help"));
	    String str;
	    while ((str = in.readLine()) != null) {
		lines.add(str);
	    }
	    in.close();
	} catch (IOException e) { }

	return lines.toArray(new String[]{});
    }

    public void print_commands(int page) {
	String[] commands = read_commands();
	int amount = 0;

	if (page > 0) {
	    amount = (page - 1) * 7;
	} else {
	    amount = 0;
	}

	Messaging.send("&dHelp &f(&dPage &f" + (page != 0 ? page : "1") + "&d of&f " + (int)Math.ceil((double)commands.length/7D) + "&d) [] = required, () = optional:");

	try {
	    for (int i = page; i < amount + 7; i++) {
		if (commands.length > i) {
		    Messaging.send(commands[i]);
		}
	    }
	} catch (NumberFormatException ex) {
	    Messaging.send("&cNot a valid page number.");
	}
    }

    public void register_command(String command, String help) {
	if(!Commands.contains(command + help)) {
	    Commands.add(command + help);
	}
    }

    public void save_command(String command, String help) {
	if(!Commands.contains(command + " &6-&e " + help)) {
	    Commands.add(command + " &6-&e " + help);

	    File file = new File(General.directory + "general.help");

	    try {
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)), true);

		for (int i = 0; i < Commands.size(); i++) {
		    out.println((String) Commands.get(i));
		}

		out.close();
	    } catch (IOException e) {
		General.log.info("Could not write to help registry!");
	    }
	}
    }

    public void save_custom_command(String command) {
	if(!Commands.contains(command)) {
	    Commands.add(command);

	    File file = new File(General.directory + "general.help");

	    try {
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)), true);

		for (int i = 0; i < Commands.size(); i++) {
		    out.println((String) Commands.get(i));
		}

		out.close();
	    } catch (IOException e) {
		General.log.info("Could not write to help registry!");
	    }
	}
    }

    public void remove_command(String command, String help) {
	if(Commands.contains(command + " &6-&e " + help)) {
	    Commands.remove(command + " &6-&e " + help);
	} else {
	    General.log.info("Help command registry does not contain "+command+" to remove!");
	}
    }

    public void remove_custom_command(String command_line) {
	if(Commands.contains(command_line)) {
	    Commands.remove(command_line);
	} else {
	    General.log.info("Help command registry does not contain "+command_line+" to remove!");
	}
    }

    @Override
    public void onPlayerJoin(PlayerEvent event) {
        Player player = event.getPlayer();
	String[] motd = readMotd();

	if(motd == null || motd.length < 1) { return; }

	String location = (int)player.getLocation().getX() +"x, " + (int)player.getLocation().getY() +"y, " + (int)player.getLocation().getZ() +"z";
	String ip = player.getAddress().getAddress().getHostAddress();
	String balance = "";

	Plugin test = plugin.getServer().getPluginManager().getPlugin("iConomy");

	if(test != null) {
	    iConomy iConomy = (iConomy)test;
	    balance = iConomy.db.get_balance(player.getName()) + " " + iConomy.currency;
	}

	for(String line : motd) {
	    Messaging.send(
		player,
		Messaging.argument(
		    line,
		    new String[]{
			"+dname,+d",
			"+name,+n",
			"+location,+l",
			"+health,+h",
			"+ip",
			"+balance",
			"+online"
		    },
		    new String[]{ 
			player.getDisplayName(),
			player.getName(),
			location,
			Misc.string(player.getHealth()),
			ip,
			balance,
			Misc.string(plugin.getServer().getOnlinePlayers().length)
		    }
		)
	    );
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
	
	if((!event.isCancelled()) && Misc.isEither(base, "/help", "/?")) {
	    int page = 0;

	    if (split.length >= 2) {
		try {
		    page = Integer.parseInt(split[1]);
		} catch (NumberFormatException ex) {
		    Messaging.send("&cNot a valid page number."); event.setCancelled(true);
		    return;
		}
	    }

	    print_commands(page);

	    event.setCancelled(true);
	}
	
	if((!event.isCancelled()) && Misc.is(base, "/setspawn")) {
	    if (!General.Watch.permission("set-spawn", player)) {
		return;
	    }
	    
	    server.m = (int)Math.ceil(player.getLocation().getX());
	    server.o = (int)Math.ceil(player.getLocation().getZ());
	    
	    Messaging.send("&eSpawn position changed to where you are standing.");
	    event.setCancelled(true);
	}

	if((!event.isCancelled()) && Misc.is(base, "/spawn")) {
	    if (!General.Watch.permission("spawn", player)) {
		return;
	    }

	    player.teleportTo(spawn(player));
	    event.setCancelled(true);
	}

	if((!event.isCancelled()) && Misc.is(base, "/motd")) {
	    String[] motd = readMotd();

	    if(motd == null || motd.length < 1) { return; }

	    String location = (int)player.getLocation().getX() +"x, " + (int)player.getLocation().getY() +"y, " + (int)player.getLocation().getZ() +"z";
	    String ip = player.getAddress().getAddress().getHostAddress();
	    String balance = "";
	    Plugin test = plugin.getServer().getPluginManager().getPlugin("iConomy");

	    if(test != null) {
		iConomy iConomy = (iConomy)test;
		balance = iConomy.db.get_balance(player.getName()) + " " + iConomy.currency;
	    }

	    for(String line : motd) {
		Messaging.send(
		    player,
		    Messaging.argument(
			line,
			new String[]{
			    "+dname,+d",
			    "+name,+n",
			    "+location,+l",
			    "+health,+h",
			    "+ip",
			    "+balance",
			    "+online"
			},
			new String[]{
			    player.getDisplayName(),
			    player.getName(),
			    location,
			    Misc.string(player.getHealth()),
			    ip,
			    balance,
			    Misc.string(plugin.getServer().getOnlinePlayers().length)
			}
		    )
		);
	    }

	    event.setCancelled(true);
	}

	if((!event.isCancelled()) && Misc.isEither(base, "/tp", "/teleport")) {
	    if (!General.Watch.permission("teleport", player)) {
		return;
	    }

	    if (split.length < 2) {
		Messaging.send("&cCorrect usage is: /tp [player]");
		return;
	    }

	    Player who = Misc.playerMatch(split[1]);
	    Player to = null;

	    if(split.length == 3) {
		to = Misc.playerMatch(split[2]);
	    }

	    if(who == null) {
		Messaging.send("&cCannot find user.");
		return;
	    }

	    if(to == null) {
		to = who;
		who = player;
	    }

	    if (to.getName().equalsIgnoreCase(who.getName())) {
		Messaging.send("&cCannot teleport to self! It's against time law yanno.");
		return;
	    }

	    log.info(who.getName() + " teleported to " + to.getName());
	    who.teleportTo(to.getLocation());

	    event.setCancelled(true);
	}

	if((!event.isCancelled()) && Misc.isEither(base, "/s", "/tphere")) {
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

	if((!event.isCancelled()) && Misc.is(base, "/getpos")) {
	    Messaging.send("Pos X: " + player.getLocation().getX() + " Y: " + player.getLocation().getY() + " Z: " + player.getLocation().getZ());
	    Messaging.send("Rotation: " + player.getLocation().getYaw() + " Pitch: " + player.getLocation().getPitch());

	    double degreeRotation = ((player.getLocation().getYaw() - 90) % 360);

	    if (degreeRotation < 0) {
		degreeRotation += 360.0;
	    }

	    Messaging.send("Compass: " + getDirection(degreeRotation) + " (" + (Math.round(degreeRotation * 10) / 10.0) + ")");

	    event.setCancelled(true);
	}
	
	if((!event.isCancelled()) && Misc.is(base, "/compass")) {
	    double degreeRotation = ((player.getLocation().getYaw() - 90) % 360);

	    if (degreeRotation < 0) {
		degreeRotation += 360.0;
	    }

	    Messaging.send("&cCompass: " + getDirection(degreeRotation));
	    
	    event.setCancelled(true);
	}

	if((!event.isCancelled()) && Misc.isEither(base, "/afk", "/away")) {
	    if ((AFK != null || !AFK.isEmpty()) && isAFK(player)) {
		Messaging.send("&7You have been marked as back.");
		unAFK(player);
	    } else {
		Messaging.send("&7You are now currently marked as away.");
		String reason = "AFK";

		if(split.length >= 2) {
		    reason = split[1];
		}

		AFK(player, reason);
	    }

	    event.setCancelled(true);
	}

	if((!event.isCancelled()) && Misc.isEither(base, "/msg", "/tell")) {
	    if (split.length < 3) {
		Messaging.send("&cCorrect usage is: /msg [player] [message]");
		event.setCancelled(true);
		return;
	    }

	    if (isAFK(player)) {
		Messaging.send("&7This player is currently away.");
		Messaging.send("&7Reason:" + AFK.get(player));
	    }

	    Player who = Misc.playerMatch(split[1]);

	    if (who != null) {
		if (who.getName().equals(player.getName())) {
		    Messaging.send("&cYou can't message yourself!");
		    event.setCancelled(true);
		    return;
		}

		Messaging.send("(MSG) <" + player.getName() + "> " + Misc.combineSplit(2, split, " "));
		Messaging.send(who, "(MSG) <" + player.getName() + "> " + Misc.combineSplit(2, split, " "));
	    } else {
		Messaging.send("&cCouldn't find player " + split[1]);
	    }
	}

	if((!event.isCancelled()) && Misc.isEither(base, "/i", "/give")) {
	    if (!General.Watch.permission("give-items", player)) {
		return;
	    }

	    if (split.length < 2) {
		Messaging.send("&cCorrect usage is: /i [item|player] [item|amount] (amount)");
		return;
	    }

	    int item = 0;
	    int amount = 1;
	    int dataType = -1;
	    Player who = null;

	    try {
		if(split[1].contains(":")) {
		    String[] data = split[1].split(":");

		    try {
			dataType = Integer.valueOf(data[1]);
		    } catch (NumberFormatException e) {
			dataType = -1;
		    }

		    item = Integer.valueOf(data[0]);
		} else {
		    item = Integer.valueOf(split[1]);
		}
	    } catch(NumberFormatException e) {
		who = Misc.playerMatch(split[1]);
	    }

	    if(item == 0) {
		String i = "0";

		if(who == null) {
		    i = split[1];
		} else {
		    i = split[2];
		}

		if(i.contains(":")) {
		    String[] data = i.split(":");

		    try {
			dataType = Integer.valueOf(data[1]);
		    } catch (NumberFormatException e) {
			dataType = -1;
		    }

		    i = data[0];
		}

		try {
		    item = Integer.valueOf(i);
		} catch(NumberFormatException e) {
		    for (int id : General.items.keySet()) {
			if (General.items.get(id).equalsIgnoreCase(i)) {
			    item = id;
			}
		    }

		    if(item == 0) {
			Messaging.send("&cInvalid item.");
			event.setCancelled(true);
			return;
		    }
		}
	    }

	    if(split.length >= 3 && who == null) {
		try {
		    amount = Integer.valueOf(split[2]);
		} catch(NumberFormatException e) {
		    amount = 1;
		}
	    }

	    if (split.length >= 4) {
		if(who != null) {
		    try {
			amount = Integer.valueOf(split[3]);
		    } catch(NumberFormatException e) {
			amount = 1;
		    }
		} else {
		    who = Misc.playerMatch(split[3]);
		}
	    }

	    if(who == null) {
		who = player;
	    }

	    if((new ItemStack(item)).getType() == null || item == 0) {
		Messaging.send("&cInvalid item.");
		event.setCancelled(true);
		return;
	    }

	    if(dataType != -1) {
		who.getWorld().dropItem(who.getLocation(), new ItemStack(item, amount, ((byte)dataType)));
	    } else {
		who.getWorld().dropItem(who.getLocation(), new ItemStack(item, amount));
	    }
	    
	    if(who.getName().equals(player.getName())) {
		Messaging.send(who, "&2Here you go c:!");
	    } else {
		Messaging.send(who, "&2Enjoy the gift c:!");
	    }

	    event.setCancelled(true);
	}

	if((!event.isCancelled()) && Misc.is(base, "/time")) {
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

	if((!event.isCancelled()) && Misc.isEither(base, "/playerlist", "/online")) {
	    if(plugin.getServer().getOnlinePlayers() == null) {
		Messaging.send(" ");
		Messaging.send("&ePlayers &fcurrently&e online:");
		Messaging.send("&f - Just you.");
		Messaging.send(" ");
		event.setCancelled(true);
		return;
	    }

	    Player[] online = plugin.getServer().getOnlinePlayers();

	    String list = "";
	    int length = (online.length-1);
	    int on = 0;

	    for(Player current : online) {
		if(current == null) { ++on; continue; }

		list += (on >= length) ? current.getName() : current.getName() + ", ";
		++on;
	    }

	    Messaging.send(" ");
	    Messaging.send("&ePlayers &fcurrently&e online:");
	    Messaging.send(list);
	    Messaging.send(" ");

	    event.setCancelled(true);
	}

	if((!event.isCancelled()) && Misc.isEither(base, "/reload", "/reloadplugin")) {
	    if (!General.Watch.permission("manage-plugins", player)) {
		return;
	    }

	    if(split.length < 2) {
		Messaging.send("&cInvalid parameters.");
		Messaging.send("&cUsage:&f /reload [plugin]");
		event.setCancelled(true);
		return;
	    }

	    String p = split[1];
	    boolean enabled = plugin.getServer().getPluginManager().isPluginEnabled(p);
	    Plugin reloading = plugin.getServer().getPluginManager().getPlugin(p);

	    if(enabled) {
		plugin.getPluginLoader().disablePlugin(reloading);
		Messaging.send("&ePlugin [&f"+plugin+"&e] Disabled.");
		event.setCancelled(true);
		return;
	    }

	    plugin.getPluginLoader().enablePlugin(reloading);
	    Messaging.send("&ePlugin [&f"+plugin+"&e] Enabled.");

	    event.setCancelled(true);
	}

	if((!event.isCancelled()) && Misc.isEither(base, "/enable", "/enableplugin")) {
	    if (!General.Watch.permission("manage-plugins", player)) {
		return;
	    }

	    if(split.length < 2) {
		Messaging.send("&cInvalid parameters.");
		Messaging.send("&cUsage:&f /enable [plugin]");
		event.setCancelled(true);
		return;
	    }

	    String p = split[1];
	    boolean enabled = plugin.getServer().getPluginManager().isPluginEnabled(p);
	    Plugin reloading = plugin.getServer().getPluginManager().getPlugin(p);

	    if(!enabled) {
		plugin.getPluginLoader().enablePlugin(reloading);
		Messaging.send("&ePlugin [&f"+plugin+"&e] Enabled.");
		event.setCancelled(true);
		return;
	    }

	    Messaging.send("&ePlugin [&f"+plugin+"&e] is already enabled.");
	    event.setCancelled(true);
	}

	if((!event.isCancelled()) && Misc.isEither(base, "/disable", "/disableplugin")) {
	    if (!General.Watch.permission("manage-plugins", player)) {
		return;
	    }

	    if(split.length < 2) {
		Messaging.send("&cInvalid parameters.");
		Messaging.send("&cUsage:&f /disable [plugin]");
		event.setCancelled(true);
		return;
	    }

	    String p = split[1];
	    boolean enabled = plugin.getServer().getPluginManager().isPluginEnabled(p);

	    if(enabled) {
		Plugin reloading = plugin.getServer().getPluginManager().getPlugin(p);

		plugin.getPluginLoader().disablePlugin(reloading);
		Messaging.send("&ePlugin [&f"+plugin+"&e] Disabled.");
		event.setCancelled(true);
		return;
	    }

	    Messaging.send("&ePlugin [&f"+plugin+"&e] is not enabled.");
	    event.setCancelled(true);
	}

	if((!event.isCancelled()) && Misc.isEither(base, "/who", "/info")) {
	    if (!General.Watch.permission("see-player-info", player)) {
		return;
	    }

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

	    Messaging.send("&f------------------------------------------------");
	    Messaging.send("&e Information for &f"+current.getName()+"&e:");
	    Messaging.send("&f------------------------------------------------");
	    Messaging.send("&6 Username: &f" + current.getName() + " ["+bar+"&f]");
	    Messaging.send("&6 -&e Location: &f" + location);
	    Messaging.send("&6 -&e Status: &f" + ((isAFK(current)) ? "AFK ("+AFK.get(current)+")" : "Around."));
	    Messaging.send("&f------------------------------------------------");
	    event.setCancelled(true);
	}
    }
}
