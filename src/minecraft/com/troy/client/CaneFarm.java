package com.troy.client;

import java.util.ArrayList;
import java.util.Comparator;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3i;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class CaneFarm extends Module implements MovementController
{

	private Vec3 target, safePos;
	private BlockScanner scanner;

	public CaneFarm()
	{
		super(Keyboard.KEY_C);
		scanner = new BlockScanner()
		{
			@Override
			public boolean matches(World world, BlockPos pos)
			{
				return world.getBlockState(pos).getBlock() == Blocks.reeds && world.getBlockState(pos.add(0, -1, 0)).getBlock() == Blocks.dirt
						&& world.getBlockState(pos.add(0, 1, 0)).getBlock() == Blocks.reeds;
			}
		};
	}

	@Override
	public void onEnable()
	{
		findInitalTarget();
		TroyClient.getClient().addMovementController(this);
	}

	@Override
	public void onDisable()
	{
		mc.gameSettings.keyBindAttack.setPressed(false);
	}

	enum MoveState
	{
		WALK, CENTER_IN_WATER, SWIM;
	}

	private boolean swimUp = false, inWater = false;
	private MoveState state = MoveState.WALK;

	private BlockPos playerPos()
	{
		return new BlockPos(mc.thePlayer);
	}

	private long lastBreak = -1;

	@Override
	public void onTick()
	{
		mc.thePlayer.capabilities.isFlying = true;
		Block inBlock = mc.theWorld.getBlockState(playerPos()).getBlock();
		inWater = inBlock == Blocks.water || inBlock == Blocks.flowing_water;
		if (state == MoveState.WALK)
		{
			if (inWater)
			{
				state = MoveState.CENTER_IN_WATER;
			}
		}
		else if (state == MoveState.CENTER_IN_WATER)
		{
			if ((Math.abs(0.5 - mc.thePlayer.posX % 1.0) < 0.2))
			{
				state = MoveState.SWIM;
			}
		}
		else if (state == MoveState.SWIM)
		{
			if (!inWater)
			{
				state = MoveState.WALK;
			}
		}

		MovingObjectPosition pos = mc.thePlayer.rayTrace(2.0, 0.0f);
		boolean destroy = pos != null && pos.typeOfHit == MovingObjectType.BLOCK && mc.theWorld.getBlockState(pos.getBlockPos()).getBlock() == Blocks.reeds;
		mc.gameSettings.keyBindAttack.setPressed(destroy);
		if (lastBreak != -1 && (System.currentTimeMillis() - lastBreak) > 20000)
		{// If we havn't found anything for 20 seconds...
			findInitalTarget();
			lastBreak = System.currentTimeMillis();
			System.out.println("new target");
		}
		if (destroy)
		{
			lastBreak = System.currentTimeMillis();
		}

		if (scanner.isComplete() || !scanner.isScanning())
		{
			scanner.setCenter(new Vec3i(mc.thePlayer.getPositionVector()));
			scanner.setShowMatches(true);
			scanner.setRange(2, 2, 2);
			scanner.scan(mc.theWorld);
		}
		scanner.tick(mc.theWorld);
		if (scanner.getMatches().isEmpty())
		{
			target = safePos;
		}
		else if (target != null && safePos != null && target.equals(safePos))
		{
			findNewTarget();
		}

		if (target == null)
		{
			findInitalTarget();
		}
		if (target != null)
		{
			if (mc.theWorld.getBlockState(new BlockPos(target.addVector(0, 1, 0))).getBlock() != Blocks.reeds)
			{
				findNewTarget();
				System.out.println("target: " + target);
			}
		}
	}

	float getDeltaY()
	{
		return (float) Math.abs(mc.thePlayer.posY - (target.yCoord - 0.5));
	}

	@Override
	public float getStrafe()
	{
		if (target != null)
		{
			float distance = (float) (target.xCoord + 0.5f - (float) mc.thePlayer.posX);

			Block headBlock = mc.theWorld.getBlockState(playerPos().add(0, 1, 0)).getBlock();
			if (inWater && headBlock == Blocks.air)
			{
				return distance;
			}
			else if (getDeltaY() > 2.5 && state == MoveState.WALK)
			{
				return 1.0f;
			}
			if (state == MoveState.WALK || (state == MoveState.SWIM && getDeltaY() < 0.5f))
			{
				return distance;
			}
			else if (state == MoveState.CENTER_IN_WATER)
			{
				distance = (((int) mc.thePlayer.posX) + 0.5f) - (float) mc.thePlayer.posX;
				return distance;
			}
		}
		return 0;
	}

	@Override
	public float getForward()
	{
		if (target != null)
		{
			float distance = (float) (target.zCoord + 0.5f - mc.thePlayer.posZ);
			return distance;
		}
		else return 0;
	}

	@Override
	public boolean isJump()
	{
		if (target != null)
		{
			if (mc.thePlayer.posY < target.yCoord - 0.1f)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isSneak()
	{
		if (target != null)
		{
			if (mc.thePlayer.posY > target.yCoord + 0.1f)
			{
				Block under = mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.getPositionVector().addVector(0, -0.5, 0))).getBlock();
				return under == Blocks.air || under == Blocks.water || under == Blocks.flowing_water || under == Blocks.reeds && !mc.thePlayer.isCollidedVertically;
			}
		}
		return false;
	}

	@Override
	public void onGUIRender(FontRenderer renderer)
	{
		renderer.drawString(state.toString(), 10, 100, 0x000000);
	}

	private void findNewTarget()
	{
		ArrayList<BlockPos> contenders = new ArrayList<BlockPos>(scanner.getMatches());
		if (!contenders.isEmpty())
		{
			// Make distances along the z axis smaller
			final Vec3 tempTarget = new Vec3(target.xCoord, target.yCoord * 10.0f, target.zCoord / 10);
			contenders.sort(new Comparator<BlockPos>()
			{

				@Override
				public int compare(BlockPos o1, BlockPos o2)
				{
					o1 = new BlockPos(o1.getX(), o1.getY() * 10.0f, o1.getZ() / 10.0f);
					o2 = new BlockPos(o2.getX(), o2.getY() * 10.0f, o2.getZ() / 10.0f);
					double o1d = new Vec3(o1).distanceTo(tempTarget);
					double o2d = new Vec3(o2).distanceTo(tempTarget);
					return Double.compare(o1d, o2d);
				}

			});
			target = new Vec3(contenders.get(0)).addVector(0, 0.25, 0);
		}
	}

	private void findInitalTarget()
	{
		ArrayList<BlockPos> contenders = new ArrayList<BlockPos>(scanner.getMatches());
		if (!contenders.isEmpty())
		{
			contenders.sort(new Comparator<BlockPos>()
			{
				@Override
				public int compare(BlockPos o1, BlockPos o2)
				{
					return o1.distanceSq(mc.thePlayer.getPosition()) < o2.distanceSq(mc.thePlayer.getPosition()) ? -1 : +1;
				}

			});
			target = new Vec3(contenders.get(0)).addVector(0, 0.25, 0);
			System.out.println("target is " + target);
		}
	}

	@Override
	public void worldRender(float partialTicks)
	{

	}

	float lastPitch = 0.0f, lastYaw = 0.0f;

	@Override
	public float getPitch()
	{
		if (Math.random() < 0.001)
		{
			lastPitch = (float) (0.5 - Math.random());
		}
		return lastPitch;
	}

	@Override
	public float getYaw()
	{
		if (Math.random() < 0.001)
		{
			lastYaw = (float) (0.5 - Math.random());
		}
		return lastYaw;
	}

	@Override
	public boolean isControllingForward()
	{
		return true;
	}

	@Override
	public boolean isControllingStrafe()
	{
		return true;
	}

	@Override
	public boolean isControllingPitch()
	{
		return true;
	}

	@Override
	public boolean isControllingYaw()
	{
		return true;
	}

	@Override
	public boolean isFinished()
	{
		return isDisabled();
	}

}
