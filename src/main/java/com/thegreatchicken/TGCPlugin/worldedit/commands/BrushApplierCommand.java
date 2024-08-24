package com.thegreatchicken.TGCPlugin.worldedit.commands;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.fastasyncworldedit.core.command.tool.brush.ScatterBrush;
import com.fastasyncworldedit.core.command.tool.brush.ShatterBrush;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.command.tool.brush.SphereBrush;
import com.thegreatchicken.TGCPlugin.PluginLoader;
import com.thegreatchicken.TGCPlugin.worldedit.BrushApplier;

public class BrushApplierCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.isOp()) return false;
		
		if (args.length < 7) return false;
		
		World world = PluginLoader.BUKKIT_SERVER.getWorld(args[0]);
		
		double x = Double.parseDouble(args[1]);
		double y = Double.parseDouble(args[2]);
		double z = Double.parseDouble(args[3]);
		
		Location location = new Location(world, x, y, z);
		
		String mask = args[4];
		String patt = args[5];
		
		String name = args[6];
		
		Brush brush = null;
		int size    = 0;
		
		switch (name) {
			case "shatter": {
				int radius = 10;
				int count  = 10;
				if (args.length >= 8)
					radius = Integer.parseInt(args[7]);
				if (args.length >= 9)
					count = Integer.parseInt(args[8]);
				
				size = radius;
				brush = new ShatterBrush(count);
				break ;
			}
			case "sphere": {
				int radius = 10;
				
				if (args.length >= 8)
					radius = Integer.parseInt(args[7]);
				
				size = radius;
				brush = new SphereBrush();
				break ;
			}
			default:
				throw new IllegalArgumentException();
		}
		
		BrushApplier.ABrush aBrush = new BrushApplier.ABrush(brush, size, mask, patt);
		
		boolean worked = BrushApplier.applyBrush(aBrush, location);
		
		return worked;
	}

}
