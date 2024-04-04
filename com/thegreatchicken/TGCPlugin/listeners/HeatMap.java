package com.thegreatchicken.TGCPlugin.listeners;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.thegreatchicken.TGCPlugin.PluginLoader;

class HeatMapLocation {

	public double x, y, z, yaw, pitch;
	
	public HeatMapLocation (Location location) {
		x = location.getX();
		y = location.getY();
		z = location.getZ();
		
		yaw   = location.getYaw();
		pitch = location.getPitch();
	}
	public boolean isAlmostEqual (HeatMapLocation newLocation) {
		double dx   = x - newLocation.x;
		double dy   = y - newLocation.y;
		double dz   = z - newLocation.z;
		double dyaw = yaw - newLocation.yaw;
		double dpit = pitch - newLocation.pitch;
		
		return Math.abs(dx) < 0.25
			&& Math.abs(dy) < 0.25
			&& Math.abs(dz) < 0.25
			&& Math.abs(dyaw) < 0.25
			&& Math.abs(dpit) < 0.25;
	}
	public String getString (double var) {
		return String.valueOf(Math.round(var * 10) / 10.0);
	}
	public String toString () {
		return getString(x) + " " + getString(y) + " " + getString(z) + " "
		     + getString(yaw) + " " + getString(pitch);
	}
}

public class HeatMap implements Runnable {
	
	public void runLater () {
		PluginLoader.BUKKIT_SERVER	
			.getScheduler()
			.runTaskLater(PluginLoader.PLUGIN, this, 2);
	}
		
	@Override
	public void run() {
		this.save(PluginLoader.BUKKIT_SERVER.getOnlinePlayers());
			
		this.runLater();
	}
	
	public static final String HEATMAP_FILE   = "plugins/tgc/heatmap.txt";
	public static final String HEATMAP_FOLDER = "plugins/tgc";
	
	public static void create () {
		File folder = new File(HEATMAP_FOLDER);
		if (!folder.exists()) {
			try {
				folder.mkdirs();
			} catch (Exception e) {
				e.printStackTrace();
				return ;
			}
		}
		
		File file = new File(HEATMAP_FILE);
		if (file.exists()) return ;
		
		try {
			file.createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static StringBuffer buffer = new StringBuffer();
	private static HashMap<Player, HeatMapLocation> lastLocations = new HashMap<>();
	private static HashMap<Player, Integer> indices               = new HashMap<>();
	
	public static void save (Collection<? extends Player> players) {
		ArrayList<Player> needsUpdate = new ArrayList<>();
		for (Player player : players) {
			HeatMapLocation location = new HeatMapLocation(player.getLocation());
			
			if (!indices.containsKey(player))
				indices.put(player, indices.size());
			HeatMapLocation lastLoc = lastLocations.getOrDefault(player, null);
			if (lastLoc != null && location.isAlmostEqual(lastLoc))
				continue ;
			
			lastLocations.put(player, location);
			needsUpdate.add(player);
		}
		
		buffer.append(needsUpdate.size());
		buffer.append("\n");
		
		for (Player player : needsUpdate) {
			HeatMapLocation location = lastLocations.get(player);
			buffer.append(indices.get(player));
			buffer.append(" ");
			buffer.append(location.toString());
			buffer.append("\n");
		}
		
		if (buffer.length() > 4096)
			_save();
	}
	public static void _save () {
		create();
		
		String data = buffer.toString();
		try {
			Files.write(Paths.get(HEATMAP_FILE), data.getBytes(), StandardOpenOption.APPEND);
		} catch (Exception e) {
			e.printStackTrace();
			return ;
		}
		
		buffer.setLength(0);
	}
	
}
