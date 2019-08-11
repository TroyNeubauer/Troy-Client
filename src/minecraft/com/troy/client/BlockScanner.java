package com.troy.client;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3i;
import net.minecraft.world.World;

public abstract class BlockScanner {
	private static final int MAX_CHUNK_DISTANCE = 64 / 16;

	private Vector2f lastChunk = null;
	private List<BlockPos> matches = new ArrayList<BlockPos>(), finishedMatches = new ArrayList<BlockPos>();

	// Scan range in chunks
	private int xRange, yRange, zRange;
	private Vec3i center;
	private boolean complete = false, scanning = false;
	private World world;

	private boolean showMatches = false;

	public abstract boolean matches(World world, BlockPos pos);

	public void tick(World world) {
		if (scanning && !complete) {
			scanChunk(lastChunk);

			lastChunk.x++;
			if (lastChunk.x > xRange) {
				lastChunk.x = -xRange;
				lastChunk.y++;
				if (lastChunk.y > zRange) {
					lastChunk.y = -zRange;
					complete = true;
				}
			}
		}
	}

	private void scanChunk(Vector2f chunk) {
		for (int y = -yRange * 16; y <= yRange * 16; y++) {
			for (int x = 0; x < 16; x++) {
				for (int z = 0; z < 16; z++) {
					BlockPos pos = new BlockPos(chunk.x * 16 + x + center.getX(), center.getY() + y,
							chunk.getY() * 16 + z + center.getZ());
					if (matches(world, pos)) {
						matches.add(pos);
					}
				}
			}
		}
	}

	public void render(float partialTicks) {

		if (showMatches) {
			Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
			// Interpolating everything back to 0,0,0. These are transforms you can find at
			// RenderEntity class
			double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
			double d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
			double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;
			// Apply 0-our transforms to set everything back to 0,0,0
			WorldRenderer renderer = Tessellator.getInstance().getWorldRenderer();
			renderer.setTranslation(-d0, -d1, -d2);
			GL11.glPushMatrix();
			GL11.glEnable(3042);
			GL11.glBlendFunc(770, 771);
			GL11.glLineWidth(1.5F);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_LINE_SMOOTH);
			GL11.glDisable(2929);
			GL11.glDepthMask(false);
			GL11.glColor4d(0.0f, 0.0f, 0.0f, 0.185F);

			for (BlockPos pos : finishedMatches) {
				RendererUtil.renderBox(renderer, pos);
			}

			GL11.glLineWidth(2.0F);
			GL11.glDisable(GL11.GL_LINE_SMOOTH);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(2929);
			GL11.glDepthMask(true);
			GL11.glDisable(3042);
			GL11.glPopMatrix();
			
			renderer.setTranslation(0, 0, 0);
		}
	}

	public void scan(World world) {
		if (lastChunk != null) {
			this.world = world;
			complete = false;
			scanning = true;
			System.out.println("matches: " + matches.size());
			finishedMatches = new ArrayList<BlockPos>(matches);
			matches.clear();
		}
	}

	// Range in terms of chunks
	public void setRange(int xRange, int yRange, int zRange) {
		this.xRange = xRange;
		this.yRange = yRange;
		this.zRange = zRange;

		this.lastChunk = new Vector2f(-xRange, -zRange);
	}

	public List<BlockPos> getMatches() {
		return finishedMatches;
	}

	public void setCenter(Vec3i center) {
		this.center = center;
	}

	public boolean isComplete() {
		return complete;
	}

	public boolean isScanning() {
		return scanning;
	}

	public void setShowMatches(boolean showMatches) {
		this.showMatches = showMatches;
	}
}
