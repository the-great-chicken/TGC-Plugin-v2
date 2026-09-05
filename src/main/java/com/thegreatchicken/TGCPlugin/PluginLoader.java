package com.thegreatchicken.TGCPlugin;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.EventManager;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.thegreatchicken.TGCPlugin.glow.GlowCommand;
import com.thegreatchicken.TGCPlugin.glow.GlowListener;
import com.thegreatchicken.TGCPlugin.glow.GlowPacketListener;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.Getter;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.thegreatchicken.TGCPlugin.inventory.InventoryListener;
import com.thegreatchicken.TGCPlugin.inventory.InventoryValidator;
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

@Getter
public class PluginLoader extends JavaPlugin {

    private com.thegreatchicken.TGCPlugin.cosmetics.CosmeticHttpServer cosmeticHttpServer;
	
	public static Server BUKKIT_SERVER;
	public static PluginLoader PLUGIN ;
	
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
	public void onLoad() {
		PLUGIN = this;
	}

	@Override
	public void onEnable () {
		this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,command -> {
			GlowCommand.commandRegister(command.registrar());
		});
		EventManager events = PacketEvents.getAPI().getEventManager();
		events.registerListener(new GlowPacketListener(), PacketListenerPriority.NORMAL);

		running_procedure("LOAD_SERVER");
		BUKKIT_SERVER = this.getServer();
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

		//this.getCommand("targetglow").setExecutor(new TargetGlowCommand());
		//this.getCommand("removeglow").setExecutor(new RemoveGlowCommand());
		//this.getCommand("useglow"   ).setExecutor(new UseGlowCommand());
		//this.getCommand("glow"      ).setExecutor(new GlowCommand   ());
		end_procedure();
		
		running_procedure("LOAD_PACKET_LISTENER");
		
		end_procedure();
		
		running_procedure("ADD_EVENT_LISTENERS");
		PluginManager manager = BUKKIT_SERVER.getPluginManager();
		
		manager.registerEvents(new LoginListener(), PLUGIN);
		manager.registerEvents(new InventoryListener(), PLUGIN);
		manager.registerEvents(new DeathListener(), PLUGIN);
		manager.registerEvents(new ClearPreprocessor(), PLUGIN);
		manager.registerEvents(new GlowListener(), PLUGIN);
		
		InventoryValidator validator = new InventoryValidator();
		validator.runLater();
		
		HeatMap heatmap = new HeatMap();
		heatmap.runLater();
		end_procedure();
		
		running_procedure("GLOW_CONFIG");
		this.saveDefaultConfig();
		end_procedure();
		if (getConfig().getBoolean("cosmetics.enabled", false)) {
			try {
				cosmeticHttpServer = com.thegreatchicken.TGCPlugin.cosmetics.CosmeticHttpServer.start(this);
				getLogger().info("Private cosmetic bridge started");
			} catch (java.io.IOException | RuntimeException e) {
				getLogger().severe("Cosmetic bridge disabled: " + e.getMessage());
			}
		}
	}
	@Override
	public void onDisable () {
		if (cosmeticHttpServer != null) cosmeticHttpServer.close();
		running_procedure("SAVE_WARP");
		WarpManager.save();
		end_procedure();
		
		running_procedure("SAVE_HEATMAP");
		HeatMap._save();
		end_procedure();
	}
	
}
