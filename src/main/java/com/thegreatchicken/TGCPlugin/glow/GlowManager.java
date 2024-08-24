package com.thegreatchicken.TGCPlugin.glow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.thegreatchicken.TGCPlugin.PluginLoader;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class GlowManager {

	public static long    COOLDOWN_DURATION = 1000 * 20;
	public static int     GLOWING_DURATION  = 3;
	public static double  DISTANCE_TRIGGER  = 10;
	public static boolean USE_GLOW_ENABLED  = true;
	
	public static final String GLOW_FILE = "plugins/tgc/glow.txt";
	
	public static ArrayList<GlowAction> pendingActions = new ArrayList<GlowAction>();
	
	public static void show (Player client) {
		PluginLoader.info("=== GLOW_ACTIONS " + client.getName() + " ===");
		
		for (GlowAction action : pendingActions)
			PluginLoader.info("client(" + action.client.getName() + ") << glow(" + action.entity.getName() + ", " + (action.endTime - System.currentTimeMillis()) + "ms);");
	}
	public static void init () {
		File file = new File(GLOW_FILE);
		if (!file.exists()) {
			PluginLoader.config("The file '" + GLOW_FILE + "' could not be found");
			
			return ;
		}
		
		FileReader reader;
		try {
			reader = new FileReader(file);
		} catch (FileNotFoundException e) {
			PluginLoader.danger("Could not read file '" + GLOW_FILE + "'");
			return ;
		}
		
		Scanner scanner = new Scanner(reader);
		
		try {
			COOLDOWN_DURATION = scanner.nextInt();
			GLOWING_DURATION  = scanner.nextInt();
			DISTANCE_TRIGGER  = scanner.nextDouble();
			
			PluginLoader.config("COOLDOWN = " + COOLDOWN_DURATION);
			PluginLoader.config("GLOWING  = " + GLOWING_DURATION);
			PluginLoader.config("DISTANCE = " + DISTANCE_TRIGGER);
			
			COOLDOWN_DURATION *= 1000;
		} catch (Exception e) {
			PluginLoader.danger("The file '" + GLOW_FILE + "' has invalid metadata");
			PluginLoader.danger(" - Expected 2 integers and one double");
			PluginLoader.danger(" - These are in order, cooldown, duration and distance");
			return ;
		}
	}
	public static void removeClosed () {
		ArrayList<GlowAction> toRemove = new ArrayList<>();
		
		long time = System.currentTimeMillis();
		for (GlowAction action : pendingActions)
			if (time > action.endTime)
				toRemove.add(action);
		
		for (GlowAction action : toRemove)
			pendingActions.remove(action);
	}
	
	public static void addGlow (Player client, LivingEntity entity, String color) {
		setGlow(client, entity, color, (byte) 0x40);
	}
	public static void setGlow (Player client, LivingEntity entity, String color, byte b) {
		try {
			PluginLoader.PLUGIN.glowingEntities.setGlowing((Entity) entity, client, ChatColor.valueOf(color));;
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
	}
	public static void sendPotionEffect (Player client, Player entity, int duration, String color) {
		GlowAction action = new GlowAction(client, entity, color, duration * 1000 + System.currentTimeMillis());
		pendingActions.add(action);

		addGlow(client, entity, color);
	}
	
	public static HashMap<Player, Long> glowCooldown = new HashMap();
	public static HashMap<Player, Long> softCooldown = new HashMap();
	public static long getVisibleCooldown (Player player) {
		long cooldownEnd = glowCooldown.getOrDefault(player, 0l);
		
		return (cooldownEnd - System.currentTimeMillis())/1000;
	}
	public static boolean isInCooldown (Player player) {
		long cooldownEnd = glowCooldown.getOrDefault(player, 0l);
	
		return cooldownEnd >= System.currentTimeMillis();
	}
	public static boolean isInSoftCooldown (Player player) {
		long cooldownEnd = softCooldown.getOrDefault(player, 0l);
	
		return cooldownEnd >= System.currentTimeMillis();
	}
	public static void startCooldown (Player player) {
		glowCooldown.put(player, System.currentTimeMillis() + COOLDOWN_DURATION);
	}
	public static void startSoftCooldown (Player player) {
		softCooldown.put(player, System.currentTimeMillis() + 500);
	}
	public static boolean useGlow (Player player) {
		if (!USE_GLOW_ENABLED) {
			player.sendMessage(ChatColor.RED + "Vous ne pouvez pas " + ChatColor.AQUA + "" + ChatColor.BOLD + "traquer" + ChatColor.RESET + "" + ChatColor.RED + " les poulets actuellement.");
			return false;
		}
		if (isInSoftCooldown(player)) return false;
		startSoftCooldown(player);
		
		if (isInCooldown(player)) {
			long duration = getVisibleCooldown(player);
			player.spigot()
				  .sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "En cooldown pendant " + ChatColor.BOLD + "" + duration + "" + ChatColor.RESET + "" + ChatColor.RED + " seconde" + (duration == 1 ? "" : "s")));

			return false;
		}
		startCooldown(player);
		
		Server server = PluginLoader.BUKKIT_SERVER;
		
		ArrayList<Player> glowingPlayers = new ArrayList<>();
		
		for (Player other : server.getOnlinePlayers()) {
			if (other == player) continue ;
			
			double dx = other.getLocation().getX() - player.getLocation().getX();
			double dy = other.getLocation().getY() - player.getLocation().getY();
			double dz = other.getLocation().getZ() - player.getLocation().getZ();
			if (dx * dx + dy * dy + dz * dz < DISTANCE_TRIGGER * DISTANCE_TRIGGER)
				continue ;
			
			glowingPlayers.add(other);
		}
		
		for (Player other : glowingPlayers)
			sendPotionEffect(player, other, GLOWING_DURATION, "WHITE");
		
		return true;
	}
	
}
