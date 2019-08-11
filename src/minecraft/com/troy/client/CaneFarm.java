package com.troy.client;

import java.util.ArrayList;
import java.util.Comparator;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3i;
import net.minecraft.world.World;

public class CaneFarm extends Module implements MovementController
{

	private BlockPos target;
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
	public void onTick()
	{
		if (scanner.isComplete() || !scanner.isScanning())
		{

			scanner.setCenter(new Vec3i(mc.thePlayer.getPositionVector()));
			scanner.setShowMatches(true);
			scanner.setRange(2, 2, 2);
			scanner.scan(mc.theWorld);
		}
		scanner.tick(mc.theWorld);

		if (target == null)
		{
			findInitalTarget();
		}
		if (target != null)
		{
			if (mc.theWorld.getBlockState(target.add(0, 1, 0)).getBlock() != Blocks.reeds)
			{
				findNewTarget();
				System.out.println("target: " + target);
			}
		}
	}

	private void findNewTarget()
	{
		ArrayList<BlockPos> contenders = new ArrayList<BlockPos>(scanner.getMatches());
		if (!contenders.isEmpty())
		{
			// Make distances along the z axis smaller
			final BlockPos tempTarget = new BlockPos(target.getX(), target.getY() * 10.0f, target.getZ() / 10);
			contenders.sort(new Comparator<BlockPos>()
			{

				@Override
				public int compare(BlockPos o1, BlockPos o2)
				{
					o1 = new BlockPos(o1.getX(), o1.getY(), o1.getZ());
					o2 = new BlockPos(o2.getX(), o2.getY(), o2.getZ());
					double o1d = o1.distanceSq(tempTarget);
					double o2d = o2.distanceSq(tempTarget);
					return Double.compare(o1d, o2d);
				}

			});
			target = contenders.get(0);
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
			target = contenders.get(0);
			System.out.println("target is " + target);
		}
	}

	@Override
	public void worldRender(float partialTicks)
	{

	}

	private BlockPos playerPos()
	{
		return new BlockPos(mc.thePlayer);
	}

	private boolean inWater()
	{
		Block inBlock = mc.theWorld.getBlockState(playerPos()).getBlock();
		return inBlock == Blocks.water || inBlock == Blocks.flowing_water;
	}

	@Override
	public float getStrafe()
	{
		System.out.println(mc.thePlayer.getPosition());
		if (target != null)
		{
			if (isJump()) return 0.0f;
			if (Math.abs(mc.thePlayer.posY - target.getY()) > 2.0)
			{
				if (inWater())
				{
					return Math.round(mc.thePlayer.posX) + 0.5f - (float) mc.thePlayer.posX;
				}
			}
			float distance = target.getX() + 0.5f - (float) mc.thePlayer.posX;
			System.out.println("x: " + distance);
			return distance;
		}
		else return 0;
	}

	@Override
	public float getForward()
	{
		if (target != null)
		{
			float distance = target.getZ() + 0.5f - (float) mc.thePlayer.posZ;
			System.out.println("z: " + distance);
			return distance;
		}
		else return 0;
	}

	@Override
	public float getPitch()
	{
		return 0;
	}

	@Override
	public float getYaw()
	{
		return 0;
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
	public boolean isJump()
	{
		if (target != null)
		{
			Block inBlock = mc.theWorld.getBlockState(playerPos()).getBlock();
			boolean inWater = inBlock == Blocks.water || inBlock == Blocks.flowing_water;
			System.out.println("in water " + inWater);
			if (inWater && target.getY() + 1 > mc.thePlayer.posY)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isSneak()
	{
		return false;
	}

	@Override
	public boolean isFinished()
	{
		return isDisabled();
	}

}
