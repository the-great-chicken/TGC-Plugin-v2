package com.thegreatchicken.TGCPlugin.warp;

import java.io.IOException;
import java.io.Writer;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import com.thegreatchicken.TGCPlugin.PluginLoader;

public class Warp {
	public boolean enabled = true;
	public final String name;
	public final String world_name;
	public final float x, y, z, yaw, pitch;
	
	private final Location warp_pos;
	
	public Warp (
			String name, String world_name, 
			float x, float y, float z, float yaw, float pitch
		) {
		this.name       = name;
		this.world_name = world_name;
		
		this.x = x;
		this.y = y;
		this.z = z;
		
		this.yaw   = yaw;
		this.pitch = pitch;
		
		World world = PluginLoader.BUKKIT_SERVER.getWorld(this.world_name);
		if (world != null)
			this.warp_pos = new Location(world, x, y, z, yaw, pitch);
		else this.warp_pos = null;
		
		if (this.warp_pos == null)
			PluginLoader.danger("Warp '" + this.name + "' has invalid world : '" + this.world_name + "'");
	}
	
	public boolean warp (Player player) {
		if (this.warp_pos == null) {
			PluginLoader.danger("Warp '" + this.name + "' has invalid world : '" + this.world_name + "'");
			player.sendMessage("The warp configuration is invalid, please contact the server owner");
			return false;
		}
		if (!enabled) {
			player.sendMessage("Un événement est en cours, réessayez lorsque celui-ci est terminé.");
			return false;
		}
		
		player.teleport(this.warp_pos);
		System.out.println("Warp successfull to position " + this.warp_pos.toString());
		player.getInventory().clear();
		
		for (PotionEffect effect : player.getActivePotionEffects())
			player.removePotionEffect(effect.getType());
	
		return true;
	}
	
	public void save (Writer writer) {
		try {
			writer.write(this.name + " " + this.world_name + " " + this.x + " " + this.y + " " + this.z + " " + this.yaw + " " + this.pitch + "\n");
		} catch (IOException e) {
			PluginLoader.danger("Could not save warp to writer");
		}
	}
	public static Warp read (String line) {
		String[] data = line.split(" ");
		if (data.length != 7) {
			PluginLoader.danger("Could not read warp, the word count is invalid");
			return null;
		}
		
		String name       = data[0];
		String world_name = data[1];
		if (name.trim()      .equals("")
		 || world_name.trim().equals("")) {
			PluginLoader.danger("Could not read warp, one of the names is empty");
			return null;
		}
		
		float x, y, z, yaw, pitch;
		
		try {
			x = Float.parseFloat(data[2]);
			y = Float.parseFloat(data[3]);
			z = Float.parseFloat(data[4]);
			
			yaw   = Float.parseFloat(data[5]);
			pitch = Float.parseFloat(data[6]);
		} catch (Exception exception) {
			PluginLoader.danger("Could not read warp, one of the coordinates isn't a number");
			return null;
		}
		
		return new Warp(name, world_name, x, y, z, yaw, pitch);
	}
}
