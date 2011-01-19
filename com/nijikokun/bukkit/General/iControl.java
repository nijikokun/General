package com.nijikokun.bukkit.General;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

/**
 * General 1.x
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
 * iControl.java
 * Permission handler
 *
 * @author Nijiko
 */
public class iControl {

    private Map<String, Set<String>> UserPermissions;
    private Configuration config;

    public iControl(Configuration config) {
	this.config = config;
    }

    public void load() {
	List<String> userKeys = this.config.getKeys("permissions.users");
	Set Permissions = new HashSet();
	List permissions;

	if (userKeys != null) {
	    for (String key : userKeys) {
		Permissions = new HashSet();
		permissions = this.config.getStringList("users." + key + ".permissions", null);

		if (permissions.size() > 0) {
		    Permissions.addAll(permissions);
		}

		this.UserPermissions.put(key.toLowerCase(), Permissions);
	    }
	}
    }

    public boolean permission(String controller, Player player) {
	Set Permissions = (Set) this.UserPermissions.get(player.getName().toLowerCase());

	if (Permissions == null) {
	    return false;
	}

	return Permissions.contains(controller);
    }
}
