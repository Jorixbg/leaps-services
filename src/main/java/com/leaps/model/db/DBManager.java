package com.leaps.model.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBManager {
	
	public enum ColumnNames{ users }
	private final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	
	private final String USER = "root";
	private final String PASS = "LeapsRoot";
	private final String HOSTNAME = "leaps.carvx2s9bjqi.eu-central-1.rds.amazonaws.com";
	private static final String DB_NAME = "leaps"; // Data Base name
	private static final String PORT = "3306";
	
	private final String DB_URL = "jdbc:mysql://localhost:8806/" + DB_NAME + "?autoReconnect=true&useSSL=false?useUnicode=true&characterEncoding=utf-8";

	
	// LOCAL DATABASE CONFIGURATION
	boolean useLocal = false;
	
	
	private Connection conn = null;
	
	private static DBManager instance = null;
	
	private DBManager() {
		if (useLocal) {
			try {
				Class.forName(JDBC_DRIVER);
				System.out.println("Driver loaded successfully");
			} catch (ClassNotFoundException e) {
				System.out.println("No such driver imported");
			}
			
			try {
				conn = DriverManager.getConnection(DB_URL, "root", "admin");
				System.out.println("Connection to database was successfully");
				
			} catch (SQLException e) {
				System.out.println("Something went wrong with the connection to the database: "  + e.getMessage());
			}
		} else {
			try {
				Class.forName(JDBC_DRIVER);
				String jdbcUrl = "jdbc:mysql://" + HOSTNAME + ":" + PORT + "/" + DB_NAME + "?user=" + USER + "&password=" + PASS + "&autoReconnect=true&failOverReadOnly=false&maxReconnects=10&useUnicode=true&characterEncoding=utf-8";
				conn = DriverManager.getConnection(jdbcUrl);
				System.out.println("Connection to database was successfully");
			} catch (ClassNotFoundException e) { 
				System.out.println("No such driver imported");
			} catch (SQLException e) { 
				System.out.println("Something went wrong with the connection to the database: "  + e.getMessage());
			}
		}
	}
	
	public static DBManager getInstance() {
		if(instance == null) {
	         instance = new DBManager();
	      }
	      return instance;
	}
	
	public Connection getConnection() {
		return conn;
	}
	
	public void closeConnection() {
		try {
			this.conn.close();
			System.out.println("Connection closed!");
		} catch (SQLException e) {
			System.out.println("Problem while closing the connection: " + e.getMessage());
		}
	}

	public static String getDbName() {
		return DB_NAME;
	}
}
