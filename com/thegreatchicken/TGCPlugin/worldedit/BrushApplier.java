package com.thegreatchicken.TGCPlugin.worldedit;

import org.bukkit.entity.Player;

import com.fastasyncworldedit.core.command.tool.brush.BrushSettings;
import com.fastasyncworldedit.core.extent.ResettableExtent;
import com.fastasyncworldedit.core.function.pattern.PatternTraverser;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.request.Request;
import com.sk89q.worldedit.util.Location;
import com.thegreatchicken.TGCPlugin.PluginLoader;

public class BrushApplier {
	
	public static class ABrush {
		public final Brush brush;
		public final int size;
		public final String mask;
		
		public final String material;
		
		public ABrush (Brush brush, int size, String mask, String material) {
			this.brush = brush;
			this.size = size;
			this.mask = mask;
			this.material = material;
		}
		
		private BrushSettings toBrush (BukkitPlayer WE_Player) {
			BrushSettings settings = new BrushSettings();
			
			settings.setBrush( brush );
			settings.setSize(size);
			
	        ParserContext parserContext = new ParserContext();
	        parserContext.setActor(WE_Player);
	        parserContext.setWorld(WE_Player.getWorld());
	        parserContext.setSession(
	        	WorldEdit.getInstance()
	        		.getSessionManager()
	        		.get(WE_Player)
	        );
	        Mask WE_Mask = WorldEdit.getInstance()
	        		.getMaskFactory()
	        		.parseFromInput(mask, parserContext);
	        if (WE_Mask == null) {
	        	WE_Mask = new Mask() {
	        		public boolean test (BlockVector3 vector) {
	        			return true;
	        		}

					@Override
					public Mask copy() {
						return this;
					}
	        	};
	        }
	        settings.setMask(WE_Mask);
	        
	        Pattern pattern = WorldEdit.getInstance()
	        		.getPatternFactory()
	        		.parseFromInput(material, parserContext);
	        settings.setFill(pattern);
	        
	        return settings;
		}
	}
	
	@SuppressWarnings("deprecation")
	public static boolean applyBrush (ABrush aBrush, org.bukkit.Location center) {
		Player opPlayer = null;
		for (Player player : PluginLoader.BUKKIT_SERVER.getOnlinePlayers())
			if (player.isOp())
				opPlayer = player;
		if (opPlayer == null) return false;
		
		if (center == null) return false;
		Location target = BukkitAdapter.adapt(center);
		if (target == null) return false;
		
		BukkitPlayer WE_Player = new BukkitPlayer(opPlayer);
		BrushSettings settings = aBrush.toBrush(WE_Player);
		
		LocalSession session = WorldEdit.getInstance()
				.getSessionManager()
				.get(WE_Player);
		
		BrushTool tool = new BrushTool();
		
        Brush brush = settings.getBrush();
        if (brush == null)
            return false;

        try (EditSession editSession = session.createEditSession(WE_Player, settings.toString())) {
            BlockBag bag = session.getBlockBag(WE_Player);

            Request.request().setEditSession(editSession);
            Mask mask = settings.getMask();
            if (mask != null) {
                Mask existingMask = editSession.getMask();

                editSession.setMask(mask);
            }

            Mask sourceMask = settings.getSourceMask();
            if (sourceMask != null) {
                editSession.addSourceMask(sourceMask);
            }
            ResettableExtent transform = settings.getTransform();
            if (transform != null) {
                editSession.addTransform(transform);
            }
            try {
                new PatternTraverser(settings).reset(editSession);
                double size = settings.getSize();
                WorldEdit.getInstance().checkMaxBrushRadius(size);
                brush.build(editSession, target.toBlockPoint(), settings.getMaterial(), size);
            } catch (MaxChangedBlocksException e) {
            	e.printStackTrace();
            } finally {
                session.remember(editSession);
                if (bag != null) {
                    bag.flushChanges();
                }
            }
        } finally {
            Request.reset();
        }
        
        return true;
	}
	
}
