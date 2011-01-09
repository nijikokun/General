package com.nijikokun.bukkit;

import java.io.File;
import java.io.IOException;
import org.bukkit.Player;
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
     * Central Data pertaining directly to the plugin name & versioning.
     */
    public static String name = "General";
    public static String codename = "Purification";
    public static String version = "1.2";

    /**
     * Listener for the plugin system.
     */
    private final iListen l = new iListen(this);

    /**
     * Controller for permissions and security.
     */
    public static iControl Watch = new iControl();

    /**
     * Things the controller needs to watch permissions for
     */
    private final String[] watching = { "manage-plugins", "teleport", "spawn", "set-spawn", "set-time", "give-items" };

    /**
     * Default settings for the permissions
     */
    private final String[] defaults = { "admin name,", "admins name,", "*", "admins name,", "admins name,", "admins name," };

    /**
     * Miscellaneous object for various functions that don't belong anywhere else
     */
    public static Misc Misc = new Misc();

    /**
     * Internal Properties controllers
     */
    public static iProperty Settings, Logging;

    public static File Motd;

    /*
     * Variables
     */
    public static String directory = "General/", spawn = "";

    public General(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, plugin, cLoader);

        registerEvents();
	
	System.out.println(Messaging.bracketize(name) + " version " + Messaging.bracketize(version) + " ("+codename+") loaded");
    }

    public void onDisable() {
	System.out.println(Messaging.bracketize(name) + " version " + Messaging.bracketize(version) + " ("+codename+") disabled");
    }

    public void onEnable() {
	(new File(directory)).mkdir();
	Settings = new iProperty(directory + "general.settings");
	Motd = new File(directory + "general.motd");

	try {
	    Motd.createNewFile();
	} catch (IOException ex) { }
	
	setupPermissions();
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_COMMAND, l, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, l, Priority.Normal, this);
    }

    public boolean isDebugging(final Player player) {
        return false;
    }

    public void setDebugging(final Player player, final boolean value) { }

    public void setupPermissions() {
	for(int x = 0; x < watching.length; x++) {
	    Watch.add(watching[x], Settings.getString("can-" + watching[x], defaults[x]));
	}
    }
}
