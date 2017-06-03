package org.mmoon.editor.erd16;

import java.io.*;
import java.net.URL;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.shiro.config.Ini;
import org.apache.shiro.web.env.IniWebEnvironment;
import com.vaadin.ui.Notification;

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
	public static long backup_interval = 10800000;

	/**
	 * the root directory of the tomcat server instance
	 */
	private String basepath;

	/**
	 * FileInputStream to read config.properties
	 */
	private FileInputStream configReader;

	/**
	 * contains the absolute paths to all turtle source files
	 */
	public static Map<String, String> ont_sources;

	public static boolean json_found = true;

	/**
	 * load the configuration data and shiro.ini file
	 */
	public Configuration() {
		// set basepath
		basepath = "";
		try {
			if (System.getProperty("erd16.appdata.basedir") == null) {
				basepath = System.getProperty("user.home") + "/.erd16/";
			} else {
				basepath = System.getProperty("erd16.appdata.basedir");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		//set path variables and create directories
		tdb_path = basepath + "tdb/";
		ttl_path = basepath + "ttl/";
		messages_path = basepath + "messages/";
		backup_path = basepath + "backup/";
		for (String path: ImmutableList.of(tdb_path, ttl_path, messages_path, backup_path)) {
			File dir = new File(path);
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}

		//copy ontology source files to application data directory
		String json = "";
		try {
			json = Files.toString(new File(basepath + "ont-sources.json"), java.nio.charset.StandardCharsets.UTF_8);
		} catch (Exception e) {
			e.printStackTrace();
			json_found = false;
			json = "{\n" +
  			"\t\"http://mmoon.org/core/\": \"http://mmoon.org/core.ttl\",\n" +
  			"\t\"http://mmoon.org/deu/schema/og/\": \"http://mmoon.org/deu/schema/og.ttl\",\n" +
  			"\t\"http://mmoon.org/deu/inventory/og/\": \"http://mmoon.org/deu/inventory/og.ttl\"\n" +
				"}";
			File file = new File(basepath + "ont-sources.json");
			try {
				Files.write(json, file, java.nio.charset.StandardCharsets.UTF_8);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		Gson gson = new Gson();
		ont_sources = gson.fromJson(json, new TypeToken<Map<String, String>>(){}.getType());
		for (Map.Entry<String, String> entry : ont_sources.entrySet()) {
			try {
				URL url = new URL(entry.getValue());
				String new_value = basepath + "ttl" + url.getPath();
				File dest = new File(new_value);
				if (!dest.exists()) {
					new File(dest.getParent()).mkdirs();
					Resources.asByteSource(url).copyTo(Files.asByteSink(dest));
				}
				entry.setValue(new_value);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//System.out.println(ont_sources);
		//initialize TDB if necessary
		new Thread(new Runnable() {
			@Override
			public void run() {
				QueryDBSPARQL.initializeTDB();
			}
		}).start();

		//start background thread for scheduled backups
		Timer timer = new Timer(true);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					QueryDBSPARQL db = new QueryDBSPARQL();
					String fileName = "backup-" + new Date().getTime() + ".ttl";
					File file = new File(backup_path + fileName);
					file.createNewFile();
					db.convertDatabaseToTurtleFile(backup_path+fileName);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 60000, backup_interval);

		//copy default shiro.ini file to application data directory and load
		String cat_base = System.getProperty("catalina.base") + "/";
		File dest = new File(basepath + "shiro/shiro.ini");
		if (!dest.exists()) {
			new File(dest.getParent()).mkdirs();
			try {
				Files.asByteSource(new File(cat_base + "webapps/erd16-0.1/WEB-INF/classes/shiro/shiro.ini"))
					.copyTo(Files.asByteSink(dest));
			} catch (IOException e) {
					e.printStackTrace();
			}
		}
		Ini ini = new Ini();
		ini.loadFromPath(basepath + "shiro/shiro.ini");
		this.setIni(ini);
	}
}
