package com.example.vorprojekt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Properties;

import org.apache.james.mime4j.field.datetime.DateTime;
import org.apache.shiro.config.Ini;
import org.apache.shiro.web.env.IniWebEnvironment;

import com.ibm.icu.text.DateFormat;

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
	public static String message_path;

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

		String[] dirs = new String[5];
		dirs[0] = basepath + "tdb/"; //tdb_path
		dirs[1] = basepath + "ttl/"; //ttl_path
		dirs[2] = basepath + "messages/"; //message_path
		dirs[3] = basepath + "backup/"; //backup_path
		dirs[4] = basepath + "shiro/"; //shiro_path

		System.out.println("config: TDB_PATH = " + dirs[0]);
		System.out.println("config: TTL_PATH = " + dirs[1]);
		System.out.println("config: MESSAGES_PATH = " + dirs[2]);
		System.out.println("config: BACKUP_PATH = " + dirs[3]);
		System.out.println("config: SHIRO_PATH = " + dirs[4]);

		for (String path: dirs) {
			File dir = new File(path);
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}

		String[] app_files = new String[4];
		app_files[0] = "ttl/deu_inventory.ttl";
		app_files[1] = "ttl/deu_schema.ttl";
		app_files[2] = "ttl/mmoon.ttl";
		app_files[3] = "shiro/shiro.ini";

		try {
			for (String file_name: app_files) {
				File file = new File(basepath + file_name);
				if (!file.exists()) {
					String cat_base = System.getProperty("catalina.base") + "/";
					File source = new File(cat_base + "webapps/erd16-0.1/WEB-INF/classes/" + file_name);
					File dest = file;
					InputStream is = null;
					OutputStream os = null;

					is = new FileInputStream(source);
					os = new FileOutputStream(dest);
					byte[] buffer = new byte[1024];
					int length;
					while ((length = is.read(buffer)) > 0) {
						os.write(buffer, 0, length);
					}
					is.close();
					os.close();
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		tdb_path = dirs[0];
		ttl_path = dirs[1];
		message_path = dirs[2];
		backup_path = dirs[3];

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
		ini.loadFromPath(basepath + "shiro/shiro.ini");
		System.out.println("config: shiro.ini from " + basepath + "shiro.ini");
		this.setIni(ini);
	}
}
