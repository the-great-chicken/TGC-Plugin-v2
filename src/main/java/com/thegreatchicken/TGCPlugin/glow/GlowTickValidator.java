package com.thegreatchicken.TGCPlugin.glow;

import java.util.ArrayList;

import org.bukkit.ChatColor;

import com.thegreatchicken.TGCPlugin.PluginLoader;

public class GlowTickValidator implements Runnable {

	public void runLater () {
		PluginLoader.BUKKIT_SERVER
			.getScheduler()
			.runTaskLater(PluginLoader.PLUGIN, this, 1);
	}
	
	@Override
	public void run() {
		long time = System.currentTimeMillis();
		
		ArrayList<GlowAction> to_remove = new ArrayList<GlowAction>();
		
		try {
			Object[] array = GlowManager.pendingActions.toArray();
			for (Object obj_action : array) {
				GlowAction action = (GlowAction) obj_action;
				if (action.endTime >= time && !action.closed) continue ;
				
				to_remove.add(action);
				PluginLoader.glowingEntities.unsetGlowing(action.entity, action.client);
			}
			
			for (GlowAction action : to_remove)
				GlowManager.pendingActions.remove(action);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		this.runLater();
	}

}
