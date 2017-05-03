package io.puzzlebox.jigsaw.data;

/**
 * Created by sc on 5/2/17.
 */

public class ConfigurationSingleton {

	public int displayHeight = 256;
	public int displayWidth = 192;
	public int actionBarHeight = 48;
//	public int statusBarHeight = 16;

	private static final ConfigurationSingleton ourInstance = new ConfigurationSingleton();

	public static ConfigurationSingleton getInstance() {
		return ourInstance;
	}

	private ConfigurationSingleton() {
	}
}
