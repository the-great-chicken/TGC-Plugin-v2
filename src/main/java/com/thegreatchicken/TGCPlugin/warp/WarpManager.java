package com.thegreatchicken.TGCPlugin.warp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.bukkit.entity.Player;

import com.thegreatchicken.TGCPlugin.PluginLoader;
import com.thegreatchicken.TGCPlugin.inventory.InventoryManager;

public class WarpManager {
	
	private static final String WARP_FOLDER = "plugins/tgc";
	private static final String WARP_FILE   = "plugins/tgc/warps.txt";
	private static HashMap<String, Warp> warps = new HashMap<>();
	
	public static ArrayList<Warp> get () {
		ArrayList<Warp> warp_array = new ArrayList<Warp>();
		
		for (Warp warp : warps.values())
			warp_array.add(warp);
		
		return warp_array;
	}
	
	public static Warp get (String name) {
		if (warps.containsKey(name))
			return warps.get(name);
		return null;
	}
	public static void append (Warp warp) {
		warps.put(warp.name, warp);
	}
	public static Warp remove (String warp) {
		if (warps.containsKey(warp))
			return warps.remove(warp);
		return null;
	}
	public static void warp (Player player, String warp) {
		if (!warps.containsKey(warp)) {
			System.out.println("Could not find warp " + warp);
			player.sendMessage("The warp name does not exist");
			return ;
		}
		
		if (warps.get(warp).warp(player))
			InventoryManager.onWarp(player, warp);
	}
	
	public static void load () {
		File file = new File(WARP_FILE);
		if (!file.exists()) {
			PluginLoader.config("The file '" + WARP_FILE + "' could not be found");
			
			return ;
		}
		
		FileReader reader;
		try {
			reader = new FileReader(file);
		} catch (FileNotFoundException e) {
			PluginLoader.danger("Could not read file '" + WARP_FILE + "'");
			return ;
		}
		
		Scanner scanner = new Scanner(reader);
		
		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			if (line.trim().equals("")) continue ;
			
			Warp warp = Warp.read(line);
			if (warp == null) {
				PluginLoader.danger("Error in config of file '"+WARP_FILE+"', the line '" + line + "' is invalid");
				continue ;
			}
			
			warps.put(warp.name, warp);
		}
	}
	
	public static void save () {
		File file   = new File(WARP_FILE);
		File folder = new File(WARP_FOLDER);
		
		if (!file.exists()) {
			try {
				folder.mkdirs();
				file.createNewFile();
			} catch (IOException e) {
				PluginLoader.danger("Could not find and create the file '" + WARP_FILE + "'");
				return ;
			}
		}
		
		FileWriter writer;
		try {
			writer = new FileWriter(file);
		} catch (IOException e) {
			PluginLoader.danger("Could not write to file '" + WARP_FILE + "'");
			return ;
		}
		
		for (Warp warp : warps.values())
			warp.save(writer);
		
		try {
			writer.close();
		} catch (IOException e) {
			PluginLoader.danger("Could not close file '" + WARP_FILE + "'");
			return ;
		}
	}
	
}
