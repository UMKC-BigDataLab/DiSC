package edu.umkc.Util;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.umkc.Constants.DiSCConstants;

public class PropertyReader {

	public static Properties properties;
	private static final Logger logger = LogManager.getLogger(PropertyReader.class.getName());
	
	private PropertyReader() {
		
	}
	
	public static Properties getInstance() {
		if(properties == null) {
			try (FileInputStream fis = new FileInputStream(DiSCConstants.PROPERTY_FILE);
					InputStreamReader isr = new InputStreamReader(fis, "UTF-8")) {
				properties = new Properties();
				properties.load(isr);
			} catch(Exception e) {
				logger.error("PropertyReader :: getInstance :: Exception e :: " + e);
				e.printStackTrace();
			}
		}
		return properties;
	}
}
