package com.thegreatchicken.TGCPlugin;

import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.thegreatchicken.TGCPlugin.glow.GlowManager;
import com.thegreatchicken.TGCPlugin.glow.GlowTickValidator;
import com.thegreatchicken.TGCPlugin.glow.commands.GlowCommand;
import com.thegreatchicken.TGCPlugin.glow.commands.RemoveGlowCommand;
import com.thegreatchicken.TGCPlugin.glow.commands.TargetGlowCommand;
import com.thegreatchicken.TGCPlugin.glow.commands.UseGlowCommand;
import com.thegreatchicken.TGCPlugin.inventory.InventoryListener;
import com.thegreatchicken.TGCPlugin.inventory.InventoryValidator;
import com.thegreatchicken.TGCPlugin.inventory.enchantments.GlowEnchantment;
import com.thegreatchicken.TGCPlugin.listeners.ClearPreprocessor;
import com.thegreatchicken.TGCPlugin.listeners.DeathListener;
import com.thegreatchicken.TGCPlugin.listeners.HeatMap;
import com.thegreatchicken.TGCPlugin.listeners.LoginListener;
import com.thegreatchicken.TGCPlugin.warp.WarpManager;
import com.thegreatchicken.TGCPlugin.warp.commands.CreateWarpCommand;
import com.thegreatchicken.TGCPlugin.warp.commands.ListWarpCommand;
import com.thegreatchicken.TGCPlugin.warp.commands.RemoveWarpCommand;
import com.thegreatchicken.TGCPlugin.warp.commands.StatusWarpCommand;
import com.thegreatchicken.TGCPlugin.warp.commands.UseWarpCommand;
import com.thegreatchicken.TGCPlugin.worldedit.commands.BrushApplierCommand;
import com.thegreatchicken.TGCPlugin.glow.lib.GlowingEntities;

public class PluginLoader extends JavaPlugin {
	
	public static Server BUKKIT_SERVER;
	public static PluginLoader PLUGIN;
	public static GlowingEntities glowingEntities;
	
	public static void danger (String message) {
		log("DANGER", message);
	}
	public static void config (String message) {
		log("CONFIG", message);
	}
	public static void warn (String message) {
		log("WARNING", message);
	}
	public static void info (String message) {
		log("INFO", message);
	}
	public static void log (String type, String message) {
		System.out.println( "[TGCPlugin: " + type + "] " + message );
	}
	
	public static void running_procedure (String name) {
		config( "---  PROCEDURE '" + name + "' ---" );
	}
	public static void end_procedure () {
		config( "" );
	}
	
	@Override
	public void onEnable () {
		running_procedure("LOAD_SERVER");
		BUKKIT_SERVER = this.getServer();
		PLUGIN = this;
		end_procedure();

		running_procedure("LOAD_WARP");
		WarpManager.load();
		end_procedure();
		
		running_procedure("LOAD_COMMANDS");
		this.getCommand("usewarp"   ).setExecutor(new UseWarpCommand   ());
		this.getCommand("createwarp").setExecutor(new CreateWarpCommand());
		this.getCommand("removewarp").setExecutor(new RemoveWarpCommand());
		this.getCommand("statuswarp").setExecutor(new StatusWarpCommand());
		this.getCommand("listwarp"  ).setExecutor(new ListWarpCommand  ());

		this.getCommand("targetglow").setExecutor(new TargetGlowCommand());
		this.getCommand("removeglow").setExecutor(new RemoveGlowCommand());
		this.getCommand("useglow"   ).setExecutor(new UseGlowCommand());
		this.getCommand("glow"      ).setExecutor(new GlowCommand   ());

		this.getCommand("webrush").setExecutor(new BrushApplierCommand());
		end_procedure();
		
		running_procedure("LOAD_PACKET_LISTENER");
		
		glowingEntities = new GlowingEntities(this);
		
		GlowTickValidator runner = new GlowTickValidator();
		runner.runLater();
		end_procedure();
		
		running_procedure("ADD_EVENT_LISTENERS");
		PluginManager manager = BUKKIT_SERVER.getPluginManager();
		
		manager.registerEvents(new LoginListener(), PLUGIN);
		manager.registerEvents(new InventoryListener(), PLUGIN);
		manager.registerEvents(new DeathListener(), PLUGIN);
		manager.registerEvents(new ClearPreprocessor(), PLUGIN);
		
		InventoryValidator validator = new InventoryValidator();
		validator.runLater();
		
		HeatMap heatmap = new HeatMap();
		heatmap.runLater();
		end_procedure();
		
		running_procedure("ADD_ENCHANTMENTS");
		GlowEnchantment.registerEnchantment();
		end_procedure();
		
		running_procedure("GLOW_CONFIG");
		GlowManager.init();
		end_procedure();
	}
	@Override
	public void onDisable () {
		running_procedure("SAVE_WARP");
		WarpManager.save();
		end_procedure();
		
		running_procedure("SAVE_HEATMAP");
		HeatMap._save();
		end_procedure();
	}
	
}
