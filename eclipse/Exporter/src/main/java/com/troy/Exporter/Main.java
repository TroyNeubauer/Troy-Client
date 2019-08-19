package com.troy.Exporter;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;

public class Main
{
	public static void main(String[] args) throws Exception
	{
		String mcVersion = args[0];
		System.out.println("Using mc version: " + mcVersion);

		Utils.downloadFile(new File("mc.json"), "http://s3.amazonaws.com/Minecraft.Download/versions/" + mcVersion + "/" + mcVersion + ".json");
		Utils.downloadFile(new File("mc.jar"), "http://s3.amazonaws.com/Minecraft.Download/versions/" + mcVersion + "/" + mcVersion + ".jar");

		Process compile = new ProcessBuilder("python", "runtime/recompile.py").directory(new File("../../../")).start();
		print(compile);

		Process reobfuscate = new ProcessBuilder("python", "runtime/reobfuscate.py").directory(new File("../../../")).start();
		print(reobfuscate);

	}

	private static void print(Process p) throws Exception
	{
		System.err.println("Printing process: " + p.info().command().orElseGet(() -> "Unknown"));
		BufferedReader childOutput = new BufferedReader(new InputStreamReader(p.getInputStream()));
		childOutput.lines().forEach(System.out::println);
		if (p.waitFor() != 0)
		{
			System.err.println("Process failed");
			System.exit(1);
		}
		System.err.println("Process complete");
	}
}
