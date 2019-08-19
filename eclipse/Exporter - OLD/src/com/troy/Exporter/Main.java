package com.troy.Exporter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Main
{
	public static void main(String[] args) throws Exception
	{
		Process compile = new ProcessBuilder("python", "../../../runtime/recompile.py").start();
		String mcVersion = args[0];
		System.out.println("Using mc version: " + mcVersion);

		Utils.downloadFile(new File("mc.json"), "http://s3.amazonaws.com/Minecraft.Download/versions/" + mcVersion + "/" + mcVersion + ".json");
		Utils.downloadFile(new File("mc.jar"), "http://s3.amazonaws.com/Minecraft.Download/versions/" + mcVersion + "/" + mcVersion + ".jar");

		print(compile);
		if (compile.waitFor() != 0)
		{
			System.err.println("Compile failed");
			System.exit(1);
		}

		Process reobfuscate = new ProcessBuilder("python", "../../../runtime/reobfuscate.py").start();
		print(reobfuscate);
		if (reobfuscate.waitFor() != 0)
		{
			System.err.println("Reobfuscate failed");
			System.exit(1);
		}

	}

	private static void print(Process p) throws Exception
	{
		System.out.println("Printing process: " + p.info().command().orElseGet(() -> "Unknown"));
		int size = 0;
		byte[] buffer = new byte[1024];
		InputStream in = p.getInputStream();
		while ((size = in.read(buffer)) != -1)
		{
			System.out.write(buffer, 0, size);
		}
		System.out.println("Stream ended");
	}
}
