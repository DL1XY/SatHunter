package de.darc.dl1xy.sathunter.util;

public class Debug {

	public static void log(String s)
	{
		System.out.println(s);
	}

	public static void log(Object o)
	{
		Debug.log(o.toString());
	}
}
