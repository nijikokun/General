package com.nijikokun.bukkit.General;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijiko.General.ConfigurationHandler;
import com.nijiko.General.DefaultConfiguration;
import org.bukkit.plugin.Plugin;

/**
 * General 2.x
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
    public static String codename = "Mafia";
    public static String version = "2.0";

    /**
     * Listener for the plugin system.
     */
    public iListen l = new iListen(this);

    /**
     * Things the controller needs to watch permissions for
     */
    private final String[] watching = { "manage-plugins", "teleport", "spawn", "set-spawn", "set-time", "give-items", "see-player-info" };

    /**
     * Miscellaneous object for various functions that don't belong anywhere else
     */
    public static Misc Misc = new Misc();

    /*
     * Internal Properties controllers
     */
    public static iProperty Items;
    private final DefaultConfiguration config;
    public static File Motd;
    public static Permissions Permissions = null;

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

	// Attempt
	if(!(new File(getDataFolder(), "config.yml").exists())) {
	    DefaultConfiguration("config.yml");
	}

	// Gogo
	this.config = new ConfigurationHandler(getConfiguration());
	getConfiguration().load();
	this.config.load();

	// Register
        registerEvents();

	log.info(Messaging.bracketize(name) + " version " + Messaging.bracketize(version) + " ("+codename+") loaded");
    }

    public void onDisable() {
	log.info(Messaging.bracketize(name) + " version " + Messaging.bracketize(version) + " ("+codename+") disabled");
    }

    public void onEnable() {
	Motd = new File(getDataFolder() + File.separator + "general.motd");
	Items = new iProperty("items.db");

	try { Motd.createNewFile(); } catch (IOException ex) { }

	// Setup
	setupCommands();
	setupPermissions();
	setupItems();
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_COMMAND, l, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, l, Priority.Normal, this);
    }

    public void setupCommands() {
	try {
	    BufferedReader in = new BufferedReader(new FileReader(getDataFolder() + File.separator + "general.help"));
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
			l.register_custom_command("&f/rlidb|reloaditems &6-&e Reload the items.db");
			l.register_custom_command("&f/help or /? &6-&e Returns this documentation");
		}
    }

    public void setupPermissions() {
		Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
	
		if(this.Permissions == null) {
			if(test != null) {
			this.Permissions = (Permissions)test;
			} else {
			log.info(Messaging.bracketize(name) + " Permission system not enabled. Disabling plugin.");
			this.getServer().getPluginManager().disablePlugin(this);
			}
		}
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
				String left = (String)item;
				String right = (String) mappedItems.get(item);
				String id = left.trim();
				String itemName;
				//log.info("Found " + left + "=" + right + " in items.db");
				if(id.matches("[0-9]+") || id.matches("[0-9]+,[0-9]+")) {
					//log.info("matches");
					if(right.contains(",")) {
						String[] synonyms = right.split(",");
						itemName = synonyms[0].replaceAll("\\s","");
						items.put(id, itemName);
						//log.info("Added " + id + "=" + itemName);
						for(int i = 1; i < synonyms.length; i++) {
							itemName = synonyms[i].replaceAll("\\s","");
							items.put(itemName, id);
							//log.info("Added " + itemName + "=" + id);
						}
					} else {
						itemName = right.replaceAll("\\s","");
						items.put(id, itemName);
						//log.info("Added " + id + "=" + itemName);
					}
				} else {
					itemName = left.replaceAll("\\s","");
					id = right.trim();
					items.put(itemName, id);
					//log.info("Added " + itemName + "=" + id);
				}
			}
		}
    }

    private void DefaultConfiguration(String name) {
		try {
			(new File(getDataFolder(), name)).createNewFile();
		} catch (IOException ex) { }
    }
}
