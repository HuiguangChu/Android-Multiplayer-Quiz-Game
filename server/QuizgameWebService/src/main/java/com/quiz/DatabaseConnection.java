package com.quiz;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContextEvent;
import static com.quiz.Common.conn;
public class DatabaseConnection {

	//open the database connection and return the Connection object conn
		public static Connection getConnection() {     
			/*
		     // driver
			   String driver = "com.mysql.jdbc.Driver";
			// URL direction :hostname +databasename
			String url = "jdbc:mysql://mysql.stud.hig.no/s120556";
			// MySQL username
			String user = "s120556";
			// Java to MySQL password
			String password = "k72ZvQtY";
		     */
		     String driver = "com.mysql.jdbc.Driver";
			// URL direction :hostname +databasename
			String url = "jdbc:mysql://localhost/freya";
			// MySQL username
			String user = "freya";
			// Java to MySQL password
			String password = "levynite_aumous";
			
				try {
				// loading driver
				Class.forName(driver).newInstance();
				// connecting to the database
				 conn = DriverManager.getConnection(url, user, password);
				if(!conn.isClosed()){	
				System.out.println("Succeeded connecting to the Database!");
				}
		          
		    } catch (Exception e) {  
		        System.out.println("connection fails");  
		    }  
		    return conn; //  
		}  


		/**
		* Unregisters JDBC driver
		* 
		* Prevents Tomcat 7 from complaining about memory leaks.
		*/
		public void contextDestroyed(ServletContextEvent sce) {
		    // On Application Shutdown, please¡­

		   
		    // 2. Deregister Driver
		    try {
		        java.sql.Driver mySqlDriver = DriverManager.getDriver("com.mysql.jdbc.Driver");
		        DriverManager.deregisterDriver(mySqlDriver);
		    } catch (SQLException ex) {
		      System.out.println("Could not deregister driver:".concat(ex.getMessage()));
		    } 

		   
		}

}
