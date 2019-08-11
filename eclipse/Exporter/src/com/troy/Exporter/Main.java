package com.troy.Exporter;

import java.io.File;

public class Main
{
	public static void main(String[] args) throws Exception
	{
		Utils.downloadFile(new File("test.html"), "http://www.google.com");
	}
}
