package com.thegreatchicken.TGCPlugin.glow.commands;

import java.util.Collection;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.thegreatchicken.TGCPlugin.PluginLoader;
import com.thegreatchicken.TGCPlugin.glow.GlowManager;
import com.thegreatchicken.TGCPlugin.glow.containers.GlowingMaintainer;
import com.thegreatchicken.TGCPlugin.utils.Selector;

public class GlowCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.isOp()) return false;
		if (args.length != 2) return false;
		
		Collection<Player> players = Selector.selectPlayers(sender, args[0]);

		int duration;
		try {
			duration = Integer.parseInt(args[1]);
		} catch (Exception e) {
			sender.sendMessage("Expected integer as second argument");
			return false;
		}
		
		Collection<Player> allPlayers = PluginLoader.BUKKIT_SERVER.getOnlinePlayers()
			.stream().map((x) -> (Player) x).toList();

		for (Player player : players)
			GlowingMaintainer.instance()
				.addGlow(player, allPlayers, label, duration);
		
		return true;
	}

}
