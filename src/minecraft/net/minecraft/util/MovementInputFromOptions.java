package net.minecraft.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;

public class MovementInputFromOptions extends MovementInput
{
	private final GameSettings gameSettings;

	public MovementInputFromOptions(GameSettings gameSettingsIn)
	{
		this.gameSettings = gameSettingsIn;
	}

	public void updatePlayerMoveState()
	{
		if (Minecraft.getMinecraft().getClient().isControllingForward())
		{
			this.moveForward = Minecraft.getMinecraft().getClient().getForward();
		}
		else
		{
			this.moveForward = 0.0F;
			if (this.gameSettings.keyBindForward.isKeyDown())
			{
				++this.moveForward;
			}

			if (this.gameSettings.keyBindBack.isKeyDown())
			{
				--this.moveForward;
			}
		}
		if (Minecraft.getMinecraft().getClient().isControllingStrafe())
		{
			this.moveStrafe = Minecraft.getMinecraft().getClient().getStrafe();
		}
		else
		{
			this.moveStrafe = 0.0F;
			if (this.gameSettings.keyBindLeft.isKeyDown())
			{
				++this.moveStrafe;
			}

			if (this.gameSettings.keyBindRight.isKeyDown())
			{
				--this.moveStrafe;
			}
		}

		this.jump = this.gameSettings.keyBindJump.isKeyDown() || Minecraft.getMinecraft().getClient().isJump();
		this.sneak = this.gameSettings.keyBindSneak.isKeyDown() || Minecraft.getMinecraft().getClient().isSneak();

		if (this.sneak)
		{
			this.moveStrafe = (float) ((double) this.moveStrafe * 0.3D);
			this.moveForward = (float) ((double) this.moveForward * 0.3D);
		}
	}
}
