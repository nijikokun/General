package com.nijikokun.bukkit.General;

import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

/**
 *
 * @author Nijiko
 */
public class Items {

    public Items() { }

    /**
     * Returns the name of the item stored in the hashmap or the item name stored in the items.txt file in the hMod folder.
     *
     * @param id
     * @return
     */
    public static String name(int id, int data) {
		String longKey = Misc.string(id) + "," + Misc.string(data);
		if (General.items.containsKey(Misc.string(id))) {
			return Misc.camelToPhrase(General.items.get(Misc.string(id)));
		} else if (data >= 0 && General.items.containsKey(longKey)) {
			return Misc.camelToPhrase(General.items.get(longKey));
		}
	
		for (Material item : Material.values()) {
			if (item.getId() == id) {
				return Misc.camelToPhrase(item.toString());
			}
		}
	
		return Misc.camelToPhrase(Misc.string(id));
    }

    /**
     * Sets the item name inside of the items array and stores it inside our database.
     *
     * @param id
     * @param name
     */
    public static void setName(String id, String name) {
	General.items.put(id, name);
	General.Items.setString(id, name);
    }

    /**
     * Checks whether a player has the required amount of items or not.
     *
     * @param player
     * @param itemId
     * @param amount
     * @return true / false
     */
    public static boolean has(Player player, int itemId, int amount) {
	PlayerInventory inventory = player.getInventory();
	ItemStack[] items = inventory.getContents();
	int total = 0;

	for (ItemStack item : items) {
	    if ((item != null) && (item.getTypeId() == itemId) && (item.getAmount() > 0)) {
		total += item.getAmount();
	    }
	}

	return (total >= amount) ? true : false;
    }

    /**
     * Checks the getArray() for the amount total of items
     *
     * @param itemId The item we are looking for
     * @return integer - Total amount of items
     */
    public static int hasAmount(Player player) {
	PlayerInventory inventory = player.getInventory();
	ItemStack[] items = inventory.getContents();
	int amount = 0;

	for (ItemStack item : items) {
	    if ((item != null)) {
		amount += item.getAmount();
	    }
	}

	return amount;
    }

    /**
     * Checks the getArray() for the amount total of this item id.
     *
     * @param itemId The item we are looking for
     * @return integer - Total amount of itemId in the array
     */
    public static int hasAmount(Player player, int itemId) {
	PlayerInventory inventory = player.getInventory();
	ItemStack[] items = inventory.getContents();
	int amount = 0;

	for (ItemStack item : items) {
	    if ((item != null) && (item.getTypeId() == itemId)) {
		amount += item.getAmount();
	    }
	}

	return amount;
    }

    /**
     * Removes the amount of items from a player
     *
     * @param player
     * @param item
     * @param amount
     */
    public static void remove(Player player, int item, int amount) {
	PlayerInventory inventory = player.getInventory();
	ItemStack[] items = inventory.getContents();
	int counter = amount;
	int leftover = 0;

	for (int i = 0; i < 120; i++) {
	    ItemStack current = inventory.getItem(i);

	    if (current == null || current.getAmount() <= 0) {
		continue;
	    }

	    if (current.getTypeId() != item) {
		continue;
	    }

	    if (current.getAmount() > counter) {
		leftover = current.getAmount() - counter;
	    }

	    if (leftover != 0) {
		inventory.remove(i);

		if (inventory.firstEmpty() == -1) {
		    player.getWorld().dropItem(player.getLocation(), new ItemStack(item, leftover));
		} else {
		    inventory.setItem(inventory.firstEmpty(), new ItemStack(item, leftover));
		}

		counter = 0;
		break;
	    } else {
		counter -= current.getAmount();
		inventory.remove(i);
	    }
	}
    }

    /**
     * Validate the string for an item
     *
     * @param item
     * @return -1 if false, id if true.
     */
    public static int[] validate(String item) {
		int[] ret = new int[]{-1, 0};
	
		try {
			ret[0] = Integer.valueOf(item);
		} catch (NumberFormatException e) {
			String val = "";
			for (String id : General.items.keySet()) {
				if(id.equalsIgnoreCase(item)){
					val = General.items.get(id);
					//General.log.info("Equals key: " + item + "=" + val);
				} else if(General.items.get(id).equalsIgnoreCase(item)) {
					val = id;
					//General.log.info("Equals val: " + val + "=" + item);
				}
			}
			//General.log.info("Resultant name: " + (val.isEmpty()?"<empty>":val));
			if (val.contains(",")) {
				String[] split = val.split(",");
				ret[0] = Integer.valueOf(split[0]);
				ret[1] = Integer.valueOf(split[1]);
			} else {
				ret[0] = Integer.valueOf(val);
			}
	
			if (ret[0] == -1) {
				return ret;
			}
		}
	
		if (!checkID(ret[0])) {
			ret[0] = -1;
			return ret;
		} else {
			return ret;
		}
    }

    /**
     * Validate the string for an item
     *
     * @param item
     * @return -1 if false, type if true.
     */
    public static int validateGrabType(String item) {
	int itemId = -1;
	int itemType = -1;


	try {
	    itemId = Integer.valueOf(item);
	} catch (NumberFormatException e) {
	    for (String id : General.items.keySet()) {
		if (General.items.get(id).equalsIgnoreCase(item)) {
		    if (id.contains(",")) {
			itemId = Integer.valueOf(id.split(",")[0]);
			itemType = Integer.valueOf(id.split(",")[1]);
		    }
		}
	    }

	    if (itemId == -1) {
		return -1;
	    }
	}

	if (!checkID(itemId)) {
	    return -1;
	} else if (!validateType(itemId, itemType)) {
	    return -1;
	} else {
	    return itemType;
	}
    }

    public static boolean validateType(int id, int type) {
	if (type == -1 || type == 0) {
	    return true;
	}

	int itemId = -1;

	if (id == 35 || id == 351 || id == 63) {
	    if (type >= 0 && type <= 15) {
		return true;
	    }
	}

	if (id == 17) {
	    if (type >= 0 && type <= 2) {
		return true;
	    }
	}

	if (id == 91 || id == 86 || id == 67 || id == 53 || id == 77 || id == 71 || id == 64) {
	    if (type >= 0 && type <= 3) {
		return true;
	    }
	}

	if (id == 66) {
	    if (type >= 0 && type <= 9) {
		return true;
	    }
	}

	if (id == 68) {
	    if (type >= 2 && type <= 5) {
		return true;
	    }
	}

	if (id == 263) {
	    if (type == 0 || type == 1) {
		return true;
	    }
	}

	if (isDamageable(id)) {
	    return true;
	}

	return false;
    }

    public static boolean checkID(int id) {
	for (Material item : Material.values()) {
	    if (item.getId() == id) {
		return true;
	    }
	}

	return false;
    }

    public static boolean isDamageable(int id) {
    	//tools (including lighters and fishing poles) and armour
	if(id >= 256 && id <= 259) return true;
	if(id >= 267 && id <= 279) return true;
	if(id >= 283 && id <= 286) return true;
	if(id >= 290 && id <= 294) return true;
	if(id >= 298 && id <= 317) return true;
	if(id == 346) return true;
	return false;
    }
    
    public static boolean isStackable(int id) {
    	// false for tools (including buckets, bow, and lighters, but not fishing poles), food, armour, minecarts, boats, doors, and signs.
	if(id >= 256 && id <= 261) return false;
	if(id >= 267 && id <= 279) return false;
	if(id >= 282 && id <= 286) return false;
	if(id >= 290 && id <= 294) return false;
	if(id >= 297 && id <= 317) return false;
	if(id >= 322 && id <= 330) return false;
	if(id == 319 || id == 320 || id == 349 || id == 350) return false;
	if(id == 333 || id == 335 || id == 343 || id == 342) return false;
	if(id == 354 || id == 2256 || id == 2257) return false;
	return true;
    }
}