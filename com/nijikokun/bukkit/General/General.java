package com.nijikokun.bukkit.General;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;

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
    public static String codename = "Yager";
    public static String version = "1.5";

    /**
     * Listener for the plugin system.
     */
    public iListen l = new iListen(this);

    /**
     * Controller for permissions and security.
     */
    public static iControl Watch = new iControl();

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

    /**
     * Internal Properties controllers
     */
    public static iProperty Settings, Items, Logging;

    public static File Motd;

    /*
     * Variables
     */
    public static String directory = "General" + File.separator, spawn = "";
    public static HashMap<Integer, String> items;

    public General(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);

        registerEvents();

	log.info(Messaging.bracketize(name) + " version " + Messaging.bracketize(version) + " ("+codename+") loaded");
    }

    public void onDisable() {
	log.info(Messaging.bracketize(name) + " version " + Messaging.bracketize(version) + " ("+codename+") disabled");
    }

    public void onEnable() {
	(new File(directory)).mkdir();
	Settings = new iProperty(directory + "general.settings");
	Motd = new File(directory + "general.motd");
	Items = new iProperty("items.db");

	try {
	    Motd.createNewFile();
	} catch (IOException ex) { }

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
	l.save_custom_command("&f/playerlist or /online &6-&e lists players online.");
	l.save_custom_command("&f/enable|disable|reload [plugin] &6-&e Manage plugins");
	l.save_custom_command("&f/who (player) &6-&e Player information.");
	l.save_custom_command("&f/spawn &6-&e Return to spawn");
	l.save_custom_command("&f/setspawn &6-&e Change spawn to where you are");
	l.save_custom_command("&f/time (day|night|raw) &6-&e Change the time");
	l.save_custom_command("&f/me &6-&e Emote your messages");
	l.save_custom_command("&f/afk &6-&e Go away or come back");
	l.save_custom_command("&f/message|tell|m [player] [message] &6-&e Private msg");
	l.save_custom_command("&f/compass|getpos &6-&e information about position");
	l.save_custom_command("&f/help or /? &6-&e Returns this documentation");
    }

    public void setupPermissions() {
	for(int x = 0; x < watching.length; x++) {
	    Watch.add(watching[x], Settings.getString("can-" + watching[x], defaults[x]));
	}
    }

    /**
     * Setup Items
     */
    public void setupItems() {
	Map mappedItems = null;
	items = new HashMap<Integer, String>();

	try {
	    mappedItems = Items.returnMap();
	} catch (Exception ex) {
	    System.out.println(Messaging.bracketize(name + " Flatfile") + " could not open items.db!");
	}

	if(mappedItems != null) {
	    for (Object item : mappedItems.keySet()) {
		int id = Integer.valueOf((String)item);
		String itemName = (String) mappedItems.get(item);

		items.put(id, itemName);
	    }
	}
    }
}
