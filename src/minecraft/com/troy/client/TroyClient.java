package com.troy.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.network.play.client.C03PacketPlayer;

public class TroyClient implements Tickable, MovementController
{

	public static CaneFarm CANE_FARM;

	private Set<MovementController> controllers = new HashSet<MovementController>();
	private List<Module> modules = new ArrayList<Module>();
	private boolean[] keys = new boolean[256];

	private Minecraft mc;

	public TroyClient()
	{
		Display.setTitle("Troy Client 0.1.0");
		modules.add(CANE_FARM = new CaneFarm());
		for (int i = 0; i < keys.length; i++)
		{
			keys[i] = Keyboard.isKeyDown(i);
		}
		this.mc = Minecraft.getMinecraft();
	}

	public void addMovementController(MovementController controller)
	{
		controllers.add(controller);
	}

	public void onTick()
	{
		if (mc.theWorld != null)
		{

			for (int i = 0; i < keys.length; i++)
			{
				boolean down = Keyboard.isKeyDown(i);
				if (down != keys[i] && down)
				{
					for (Module module : modules)
					{
						if (module.getKey() == i)
						{
							module.toggle();
						}
					}
				}
				keys[i] = down;
			}
			for (Module module : modules)
			{
				if (module.isEnabled()) module.onTick();
			}
		}

		// Remove movement controllers that are finished
		Iterator<MovementController> it = controllers.iterator();
		while (it.hasNext())
		{
			if (it.next().isFinished()) it.remove();

		}
		boolean pitch = isControllingPitch(), yaw = isControllingYaw();
		if (pitch) mc.thePlayer.rotationPitch = getPitch();
		if (yaw) mc.thePlayer.rotationYaw = getYaw();
		if (pitch || yaw)
		{
			mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, mc.thePlayer.onGround));
		}
	}

	@Override
	public void worldRender(float partialTicks)
	{
		if (mc.theWorld != null)
		{
			for (Module module : modules)
			{
				if (module.isEnabled()) module.worldRender(partialTicks);
			}
		}
	}

	@Override
	public void onGUIRender(FontRenderer renderer)
	{
		int y = 0;
		renderer.drawString("Troy Client", 2, y, 0);
		for (Module module : modules)
		{
			y += renderer.FONT_HEIGHT + 2;

			renderer.drawString(module.getName() + " (" + Keyboard.getKeyName(module.getKey()) + ")", 2, y, module.isEnabled() ? 0x10ff00 : 0xff0000);
			if(module.isEnabled())
				module.onGUIRender(renderer);
		}
	}

	public boolean isJump()
	{
		for (MovementController controller : controllers)
		{
			if (controller.isJump()) return true;
		}
		return false;
	}

	// Causes the player to sneak when true is returned
	public boolean isSneak()
	{
		for (MovementController controller : controllers)
		{
			if (controller.isSneak()) return true;
		}
		return false;
	}

	public boolean isControllingForward()
	{
		for (MovementController controller : controllers)
		{
			if (controller.isControllingForward()) return true;
		}
		return false;
	}

	public boolean isControllingStrafe()
	{
		for (MovementController controller : controllers)
		{
			if (controller.isControllingStrafe()) return true;
		}
		return false;
	}

	public float getStrafe()
	{
		for (MovementController controller : controllers)
		{
			if (controller.isControllingStrafe()) return controller.getStrafe();
		}
		throw new RuntimeException();
	}

	public float getForward()
	{
		for (MovementController controller : controllers)
		{
			if (controller.isControllingForward()) return controller.getForward();
		}
		throw new RuntimeException();
	}

	@Override
	public boolean isControllingPitch()
	{
		for (MovementController controller : controllers)
		{
			if (controller.isControllingPitch()) return true;
		}
		return false;
	}

	@Override
	public boolean isControllingYaw()
	{
		for (MovementController controller : controllers)
		{
			if (controller.isControllingYaw()) return true;
		}
		return false;
	}

	@Override
	public float getPitch()
	{
		for (MovementController controller : controllers)
		{
			if (controller.isControllingPitch()) return controller.getPitch();
		}
		throw new RuntimeException();
	}

	@Override
	public float getYaw()
	{
		for (MovementController controller : controllers)
		{
			if (controller.isControllingYaw()) return controller.getYaw();
		}
		throw new RuntimeException();
	}

	@Override
	public boolean isFinished()
	{
		return false;
	}

	public static TroyClient getClient()
	{
		return Minecraft.getMinecraft().getClient();
	}

}
