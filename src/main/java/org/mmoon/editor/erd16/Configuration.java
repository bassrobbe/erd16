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

import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
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

		//TODO: drop the following logic to copy files from a hard-coded sources in catalina.base
		//TODO: instead, read a ontology IRI to Turtle file path mapping from a JSON file (names for example ont-sources.json)
		//TODO: fail early if such JSON mapping file is not provided
		String[] app_files = new String[4];
		app_files[0] = "ttl/deu_inventory.ttl";
		app_files[1] = "ttl/deu_schema.ttl";
		app_files[2] = "ttl/mmoon.ttl";

		try {
			for (String file_name: app_files) {
				File file = new File(basepath + file_name);
				if (!file.exists()) {
					String cat_base = System.getProperty("catalina.base") + "/";
					File source = new File(cat_base + "webapps/erd16-0.1/WEB-INF/classes/" + file_name);
					File dest = file;
					try(InputStream is = new FileInputStream(source);
						OutputStream os = new FileOutputStream(dest)) {
						byte[] buffer = new byte[1024];
						int length;
						while ((length = is.read(buffer)) > 0) {
							os.write(buffer, 0, length);
						}
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

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
