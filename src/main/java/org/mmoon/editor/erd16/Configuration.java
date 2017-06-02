package org.mmoon.editor.erd16;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.shiro.config.Ini;
import org.apache.shiro.web.env.IniWebEnvironment;

/**
 * Loads configuration data and sets up the Shiro Web Environment based on
 * shiro.ini file
 */
public class Configuration extends IniWebEnvironment {
	/**
	 * path to triple store location on disk
	 */
	public static String tdb_path;

	/**
	 * path to turtle files (raw data of triple store)
	 */
	public static String ttl_path;

	/**
	 * path to administrator messages directory
	 */
	public static String messages_path;

	/**
	 * path for scheduled backups
	 */
	public static String backup_path;

	/**
	 * time interval for backups
	 */
	public static long backup_interval;

	/**
	 * the root directory of the tomcat server instance
	 */
	private String basepath;

	/**
	 * FileInputStream to read config.properties
	 */
	private FileInputStream configReader;

	/**
	 * Properties Object to store the configurations
	 */
	private Properties config = new Properties();

	/**
	 * contains the absolute paths to all turtle source files
	 */
	public static Map<String, String> ont_sources;

	/**
	 * load the configuration data and shiro.ini file
	 */
	public Configuration() {
		backup_interval = 10800000;

		basepath = "";
		// load configuration data
		try {
			if (System.getProperty("erd16.appdata.basedir") == null) {
				basepath = System.getProperty("user.home") + "/.erd16/";
			} else {
				basepath = System.getProperty("erd16.appdata.basedir");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		tdb_path = basepath + "tdb/";
		ttl_path = basepath + "ttl/";
		messages_path = basepath + "messages/";
		backup_path = basepath + "backup/";
		String shiro_path = basepath + "shiro/";

		System.out.println("config: TDB_PATH = " + tdb_path);
		System.out.println("config: TTL_PATH = " + ttl_path);
		System.out.println("config: MESSAGES_PATH = " + messages_path);
		System.out.println("config: BACKUP_PATH = " + backup_path);
		System.out.println("config: SHIRO_PATH = " + shiro_path);

		for (String path: ImmutableList.of(tdb_path, ttl_path, messages_path, backup_path, shiro_path)) {
			File dir = new File(path);
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}

		String json = "";
		try {
			json = com.google.common.io.Files.toString(new File(basepath + "ont-sources.json"), java.nio.charset.StandardCharsets.UTF_8);
		} catch (Exception e) {
			e.printStackTrace();
		//	new Notification("Warning", "No ontology source file \"ont-sources.json found, using default preferences instead.",
		//		Notification.Type.WARNING_MESSAGE, true).show(Page.getCurrent());
			json = "{\n" +
  			"\t\"http://mmoon.org/core/\": \"http://mmoon.org/core.ttl\",\n" +
  			"\t\"http://mmoon.org/deu/schema/og/\": \"http://mmoon.org/deu/schema/og.ttl\",\n" +
  			"\t\"http://mmoon.org/deu/inventory/og/\": \"http://mmoon.org/deu/inventory/og.ttl\"\n" +
				"}";
			File file = new File(basepath + "ont-sources.json");
			try {
				com.google.common.io.Files.write(json, file, java.nio.charset.StandardCharsets.UTF_8);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		Gson gson = new Gson();
		Map<String, String> ont_sources_local = gson.fromJson(json, new TypeToken<Map<String, String>>(){}.getType());
		for (Map.Entry<String, String> entry : ont_sources_local.entrySet()) {
			try {
				URL url = new URL(entry.getValue());
				String new_value = basepath + "ttl" + url.getPath();
				File dest = new File(new_value);
				if (!dest.exists()) {
					new File(dest.getParent()).mkdirs();
					Resources.asByteSource(url).copyTo(com.google.common.io.Files.asByteSink(dest));
				}
				entry.setValue(new_value);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		ont_sources = ont_sources_local;

		// Initialize TDB if necessary
		new Thread(new Runnable() {

			@Override
			public void run() {
				QueryDBSPARQL.initializeTDB();
			}
		}).start();

		// Start background thread for scheduled backups
		Timer timer = new Timer(true);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					QueryDBSPARQL db = new QueryDBSPARQL();
					String fileName = "backup-"+new Date().getTime()+".ttl";
					File file = new File(backup_path+fileName);
					file.createNewFile();
					db.convertDatabaseToTurtleFile(backup_path+fileName);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 60000, backup_interval);

		// create new Ini object from shiro.ini
		Ini ini = new Ini();
		Path shiroIniPath = Paths.get(basepath + "shiro/shiro.ini");
		if(!Files.isRegularFile(shiroIniPath)) {
			URL shiroClassPathURL = Resources.getResource("shiro/shiro.ini");

			try {
				byte[] bytes = Resources.toByteArray(shiroClassPathURL);
				Files.write(shiroIniPath, bytes);
			} catch (IOException ioe) {
				throw new RuntimeException("error reading shiro config from classpath", ioe);
			}
		}

		ini.loadFromPath(shiroIniPath.toString());
		System.out.println("config: shiro.ini from " + shiroIniPath);
		this.setIni(ini);
	}
}
