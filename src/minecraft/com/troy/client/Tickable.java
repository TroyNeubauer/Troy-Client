package com.troy.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.world.World;

public interface Tickable {
	public void onTick();
	
	public void worldRender(float partialTicks);
	
}
