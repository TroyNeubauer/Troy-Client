package com.troy.client;

public interface MovementController
{
	//Returns true if this movement controller needs to control any of the inputs this tick.
	//If true is returned, the corresponding get method will be called to obtain the desired value
	public boolean isControllingForward();
	public boolean isControllingStrafe();
	public boolean isControllingPitch();
	public boolean isControllingYaw();
	
	public boolean isJump();
	public boolean isSneak();

	public float getStrafe();
	public float getForward();
	
	public float getPitch();
	public float getYaw();

	//Returns true if this controller no longer wants control of the player
	public boolean isFinished();

}

