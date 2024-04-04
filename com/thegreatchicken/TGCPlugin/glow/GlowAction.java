package com.thegreatchicken.TGCPlugin.glow;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class GlowAction {
	public final Player client;
	public final LivingEntity entity;
	public final String color;
	
	public final long endTime;
	public boolean closed = false;
	
	public GlowAction (Player client, LivingEntity entity, String color, long endTime) {
		this.client = client;
		this.entity = entity;
		this.color = color;
		
		this.endTime = endTime;
	}
}
