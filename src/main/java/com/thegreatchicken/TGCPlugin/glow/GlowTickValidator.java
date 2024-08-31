package com.thegreatchicken.TGCPlugin.glow;

import java.util.ArrayList;

import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.scores.PlayerTeam;

import com.thegreatchicken.TGCPlugin.PluginLoader;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R1.scoreboard.CraftScoreboard;
import org.bukkit.entity.Player;

import static net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket.createPlayerPacket;

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
			}
			
			for (GlowAction action : to_remove)
				GlowManager.pendingActions.remove(action);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		this.runLater();
	}


}
