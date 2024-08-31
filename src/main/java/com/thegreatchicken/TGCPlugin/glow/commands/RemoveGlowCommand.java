package com.thegreatchicken.TGCPlugin.glow.commands;

import com.thegreatchicken.TGCPlugin.PluginLoader;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.scores.PlayerTeam;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.thegreatchicken.TGCPlugin.glow.GlowAction;
import com.thegreatchicken.TGCPlugin.glow.GlowManager;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R1.scoreboard.CraftScoreboard;
import org.bukkit.entity.Player;

import static net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket.createPlayerPacket;

public class RemoveGlowCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.isOp()) return false;
		if (args.length != 1) return false;
		
		
		String name = args[0];
		for (GlowAction action : GlowManager.pendingActions) {
			if (action.entity.getName().equals(name))
				action.closed = true;
            try {
                PluginLoader.glowingEntities.unsetGlowing(action.entity, action.client);

				String uid =  action.entity.getUniqueId().toString();
				if (action.entity instanceof Player) {
					uid = action.entity.getName();
				}
				PlayerTeam team =((CraftScoreboard) action.client.getScoreboard()).getHandle().getPlayersTeam(uid);
				if (team != null)
					sendJoinTeamPacket(action.client,team, uid);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
            // send join team if is in it

		}
			
		return true;
	}

	private void sendJoinTeamPacket(Player player1, PlayerTeam team, String entity) {
		ServerGamePacketListenerImpl ps = ((CraftPlayer) player1).getHandle().connection;
		ps.send(createPlayerPacket(team,entity, ClientboundSetPlayerTeamPacket.Action.ADD));
	}

}
