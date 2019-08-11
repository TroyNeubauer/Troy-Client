package com.troy.client;

import net.minecraft.client.Minecraft;

public abstract class Module implements Tickable {
	private boolean enabled;
	private int key;
	
	protected Minecraft mc;

	public Module(int key) {
		this.key = key;
		this.enabled = false;
		this.mc = Minecraft.getMinecraft();
	}

	public void onEnable() {

	}

	public void onDisable() {

	}

	public final boolean isEnabled() {
		return enabled;
	}
	
	public final boolean isDisabled() {
		return !enabled;
	}

	public int getKey() {
		return key;
	}

	public String getName() {
		String start = this.getClass().getSimpleName();
		StringBuilder sb = new StringBuilder();
		if (!start.isEmpty()) {
			sb.append(start.charAt(0));
			for (int i = 1; i < start.length(); i++) {
				char c = start.charAt(i);
				if (Character.isUpperCase(c)) {
					sb.append(' ');
				}
				sb.append(c);
			}
		}

		return sb.toString();
	}

	public void enable() {
		enabled = true;
		stateChanged();
	}

	public void disable() {
		enabled = false;
		stateChanged();
	}

	public void toggle() {
		enabled = !enabled;
		stateChanged();
	}

	private void stateChanged() {
		if (enabled) {
			onEnable();
		} else {
			onDisable();
		}
	}

}
