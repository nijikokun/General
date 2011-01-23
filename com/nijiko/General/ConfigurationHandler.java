package com.nijiko.General;

import org.bukkit.util.config.Configuration;

/**
 * Handles default configuration and loads data.
 * 
 * @author Nijiko
 */
public class ConfigurationHandler extends DefaultConfiguration {
    private Configuration config;

    public ConfigurationHandler(Configuration config) {
	this.config = config;
    }

    public void load() {
	this.health = this.config.getBoolean("playerlist.show-health", this.health);
	this.coords = this.config.getBoolean("playerlist.show-coords", this.coords);
	this.commands = this.config.getBoolean("help.inject-commands", this.commands);
    }
}
