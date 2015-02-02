package de.darc.dl1xy.sathunter.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertyReader {

	private Properties auto;
	
	public PropertyReader() {
		
	}

	public Properties readProps(final String filename)
	{
		auto = new Properties();
		try {
			auto.load(new FileInputStream(filename));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return auto;
	}
	
}
