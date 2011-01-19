package com.nijikokun.bukkit.General;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import com.nijiko.ConfigurationHandler;
import com.nijiko.DefaultConfiguration;

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
public class General extends JavaPlugin {
    /*
     * Loggery Foggery
     */
    public static final Logger log = Logger.getLogger("Minecraft");

    /*
     * Central Data pertaining directly to the plugin name & versioning.
     */
    public static String name = "General";
    public static String codename = "Ninja";
    public static String version = "1.8";

    /**
     * Listener for the plugin system.
     */
    public iListen l = new iListen(this);

    /**
     * Controller for permissions and security.
     */
    public static iControl Watch;

    /**
     * Things the controller needs to watch permissions for
     */
    private final String[] watching = { "manage-plugins", "teleport", "spawn", "set-spawn", "set-time", "give-items", "see-player-info" };

    /**
     * Default settings for the permissions
     */
    private final String[] defaults = { "admin name,", "admins name,", "*", "admins name,", "admins name,", "admins name,", "*" };

    /**
     * Miscellaneous object for various functions that don't belong anywhere else
     */
    public static Misc Misc = new Misc();

    /*
     * Internal Properties controllers
     */
    public static iProperty OldSettings, Items, Logging;
    private final DefaultConfiguration config;
    public static File Motd;

    /*
     * Variables
     */
    public static String directory = "General" + File.separator, spawn = "";
    public static HashMap<String, String> items;
    public static boolean health = true, coords = true, commands = true;

    public General(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);

	// Start Registration
	folder.mkdirs();

	// Convert to new format.
	if((new File(directory)).exists()) {
	    log.info("[General] Attempting to convert...");

	    // New Configuration file.
	    DefaultConfiguration("config.yml");

	    // Setup
	    File GeneralDirectory = (new File(directory));
	    File YAMLSettings = (new File(getDataFolder(), "config.yml"));
	    File GeneralSettings = (new File(directory, "general.settings"));
	    File Motd = (new File(directory, "general.motd"));
	    File Help = (new File(directory, "general.help"));
	    
	    if(GeneralSettings.exists()) {
		OldSettings = new iProperty(directory + "general.settings");

		try {
		    log.info("[General] Converting settings..");
		    Map mappedItems = OldSettings.returnMap();

		    HashMap<String, Set<String>> YMLPermissions = new HashMap<String, Set<String>>();
		    HashMap<String, String> YMLSettings = new HashMap<String, String>();

		    for (Object k : mappedItems.keySet()) {
			String key = (String)k;
			String value = (String)mappedItems.get(key);
			boolean found = false;

			for (String perm : watching) {
			    if(key.equalsIgnoreCase("can-" + perm)) {
				if(value.contains(",")) {
				    String[] players = value.split(",");

				    for(String player : players) {
					player = player.replace(",", "");

					if(YMLPermissions.containsKey(player.toLowerCase())) {
					    YMLPermissions.get(player.toLowerCase()).add(perm);
					} else {
					    YMLPermissions.put(player.toLowerCase(), (new HashSet<String>()));
					    YMLPermissions.get(player.toLowerCase()).add(perm);
					}
				    }

				    found = true;
				    break;
				} else if(value.equals("*")) {
				    found = true;
				    break;
				}

				found = true;
				break;
			    }
			}

			if(!found) {
			    if(key.equals("who-show-health")) {
				key = "playerlist,show-health";
			    } else if(key.equals("who-show-coords")) {
				key = "playerlist,show-coords";
			    } else if(key.equals("inject-help-commands")) {
				key = "help,inject-commands";
			    }

			    YMLSettings.put(key, value);
			}
		    }

		    try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(YAMLSettings)), true);
			boolean pl = false;
			boolean hl = false;

			// Start with the settings
			for (Object k : YMLSettings.keySet()) {
			    String key = (String)k;
			    String value = (String)YMLSettings.get(key);
			    key += ",";
			    String[] keys = key.split(",");
			    int i = 0;

			    if((keys[0].equals("help") && !hl) || (keys[0].equals("playerlist") && !pl)) {
				if(keys[0].equals("help")) { hl = true; }
				if(keys[0].equals("playerlist")) { pl = true; }
				out.println(keys[0] + ":");
			    }

			    if(keys[1].equals("show-health") || keys[1].equals("show-coords") || keys[1].equals("inject-commands")) {
				out.println(Misc.repeat(' ', 4) + keys[1] + ": " + value);
			    }
			}

			out.println("");
			out.println("users:");

			// Start with the settings
			for (Object ks : YMLPermissions.keySet()) {
			    String key = (String)ks;
			    Set<String> perms = YMLPermissions.get(key);

			    int i = 0;

			    out.println(Misc.repeat(' ', 4) + key+":");

			    for(String k : perms) {
				out.println(Misc.repeat(' ', 8) + " - " + k);
			    }
			}

			out.close();
		    } catch (IOException e) {
			log.info("Could not create config.yml from settings!");
		    }

		    log.info("[General] Converted settings to yml file.. hopefully.");
		} catch (Exception ex) {
		    log.info("[General] An error occured: " + ex);
		}

		log.info("[General] Deleting old file...");
		GeneralSettings.setWritable(true);
		GeneralSettings.delete();
		log.info("[General] File deleted...");
	    }

	    if(Motd.exists()) {
		log.info("[General] Moving Motd...");
		Motd.setWritable(true);
		Motd.renameTo(new File(getDataFolder(), Motd.getName()));
		log.info("[General] Move completed.");
	    }

	    if(Help.exists()) {
		log.info("[General] Moving Help...");
		Help.setWritable(true);
		Help.renameTo(new File(getDataFolder(), Help.getName()));
		log.info("[General] Move completed.");
	    }

	    log.info("[General] Removing old diretory...");
	    GeneralDirectory.setWritable(true);
	    GeneralDirectory.delete();
	    log.info("[General] Conversion complete!");
	}

	this.config = new ConfigurationHandler(getConfiguration());
	getConfiguration().load();
	this.config.load();

        registerEvents();

	log.info(Messaging.bracketize(name) + " version " + Messaging.bracketize(version) + " ("+codename+") loaded");
    }

    public void onDisable() {
	log.info(Messaging.bracketize(name) + " version " + Messaging.bracketize(version) + " ("+codename+") disabled");
    }

    public void onEnable() {
	Motd = new File(getDataFolder() + File.separator + "general.motd");
	Items = new iProperty("items.db");

	try {
	    Motd.createNewFile();
	} catch (IOException ex) { }

	// Setup
	setupCommands();
	registerCommands();
	setupPermissions();
	setupItems();
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_COMMAND, l, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, l, Priority.Normal, this);
    }

    public void setupCommands() {
	try {
	    BufferedReader in = new BufferedReader(new FileReader(directory + "general.help"));
	    String str;

	    while ((str = in.readLine()) != null) {
		l.Commands.add(str);
	    }

	    in.close();
	} catch (IOException e) { }
    }

    public void registerCommands() {
	if(commands) {
	    l.register_custom_command("&f/online|playerlist|who &6-&e Shows player list.");
	    l.register_custom_command("&f/online|playerlist|who [player] &6-&e Shows player info.");
	    l.register_custom_command("&f/spawn &6-&e Return to spawn");
	    l.register_custom_command("&f/setspawn &6-&e Change spawn to where you are.");
	    l.register_custom_command("&f/time help &6-&e for more information.");
	    l.register_custom_command("&f/me &6-&e Emote your messages");
	    l.register_custom_command("&f/afk (message) &6-&e Go away or come back");
	    l.register_custom_command("&f/i|give [item|player] (item|amount) (amount) &6-&e Give items.");
	    l.register_custom_command("&f/message|tell|m [player] [message] &6-&e Private msg");
	    l.register_custom_command("&f/compass|getpos &6-&e information about position");
	    l.register_custom_command("&f/help or /? &6-&e Returns this documentation");
	}
    }

    public void setupPermissions() {
	Watch = new iControl(getConfiguration());
	Watch.load();
    }

    /**
     * Setup Items
     */
    public void setupItems() {
	Map mappedItems = null;
	items = new HashMap<String, String>();

	try {
	    mappedItems = Items.returnMap();
	} catch (Exception ex) {
	    System.out.println(Messaging.bracketize(name + " Flatfile") + " could not open items.db!");
	}

	if(mappedItems != null) {
	    for (Object item : mappedItems.keySet()) {
		String id = (String)item;
		String itemName = (String) mappedItems.get(item);

		items.put(id, itemName);
	    }
	}
    }

    private void DefaultConfiguration(String name) {
	try {
	    (new File(getDataFolder(), name)).createNewFile();
	} catch (IOException ex) { }
    }
}
