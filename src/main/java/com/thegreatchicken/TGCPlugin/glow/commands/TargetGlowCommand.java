package com.thegreatchicken.TGCPlugin.glow.commands;

import java.util.Collection;
import java.util.List;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.thegreatchicken.TGCPlugin.PluginLoader;
import com.thegreatchicken.TGCPlugin.glow.GlowManager;
import com.thegreatchicken.TGCPlugin.glow.containers.GlowingMaintainer;
import com.thegreatchicken.TGCPlugin.listeners.ClearPreprocessor;
import com.thegreatchicken.TGCPlugin.utils.Selector;

public class TargetGlowCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.isOp()) return false;
		if (args.length != 2 && args.length != 3) return false;
		
		String color = "WHITE";
		if (args.length == 3) color = args[2];

		Collection<Player> clients  = Selector.selectPlayers(sender, args[0]);
		Collection<Entity> entities = Selector.select       (sender, args[1]);

		//System.out.println("=== TARGET GLOW ===");
		//for (Player client : clients)
		//	System.out.println("Client " + client);
		//for (Entity entity : entities)
		//	System.out.println("Entity " + entity);

		for (Entity entity : entities) {
			if (!(entity instanceof LivingEntity)) continue ;

			LivingEntity livingEntity = (LivingEntity) entity;

			GlowingMaintainer
				.instance()
				.addGlow(livingEntity, clients, color, 0);
		}

		return true;
	}

}
