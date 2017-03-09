package nohorjo.howlserver;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Settings {
	private static Properties settings = new Properties();

	static {
		String systemProperties = System.getProperty("system.properties");
		System.out.println("Properties file: " + systemProperties);
		try (final InputStream is = new FileInputStream(systemProperties)) {
			settings.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getProperty(String key) {
		return settings.getProperty(key);
	}
}
