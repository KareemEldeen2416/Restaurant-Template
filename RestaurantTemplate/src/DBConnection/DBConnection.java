package DBConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database Connection Manager for MySQL.
 * 
 * Provides:
 * - MySQL Database Connection setup to database: "restaurant_template" on default port 3306.
 * - Automatic database creation if it does not exist.
 * - executeQuery(String query): Executes retrieval queries (SELECT) and returns a ResultSet.
 * - executeUpdate(String query): Executes non-retrieval queries (INSERT, UPDATE, DELETE, etc.) and returns the affected rows count.
 * 
 * @author KareemEldeen
 */
public class DBConnection {

    // =========================================================================
    // Database Configuration Parameters
    // =========================================================================
    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DATABASE_NAME = "restaurant_template";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    private static final String CONNECTION_PARAMS = "?useUnicode=true"
            + "&characterEncoding=UTF-8"
            + "&serverTimezone=UTC"
            + "&useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&connectTimeout=3000"
            + "&socketTimeout=5000";

    private static final String URL_WITH_DB = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE_NAME + CONNECTION_PARAMS;
    private static final String URL_SERVER_ONLY = "jdbc:mysql://" + HOST + ":" + PORT + "/" + CONNECTION_PARAMS;
    
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    private static Connection connection = null;

    /**
     * Establishes or returns the active MySQL Database Connection to "restaurant_template".
     * Automatically creates the database if it doesn't already exist on the server.
     * 
     * @return active java.sql.Connection
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName(DRIVER);

                // 1. Try to connect directly to the database
                try {
                    connection = DriverManager.getConnection(URL_WITH_DB, USERNAME, PASSWORD);
                    System.out.println("MySQL Connection Established Successfully to: " + DATABASE_NAME);
                } catch (SQLException e) {
                    // 2. If database doesn't exist (e.g. error 1049 Unknown database), create it
                    System.out.println("Connecting to MySQL server to create database '" + DATABASE_NAME + "' if missing...");
                    try (Connection serverConn = DriverManager.getConnection(URL_SERVER_ONLY, USERNAME, PASSWORD);
                         Statement stmt = serverConn.createStatement()) {
                        stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME + " DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;");
                        System.out.println("Database '" + DATABASE_NAME + "' created or verified successfully.");
                    } catch (SQLException ex) {
                        System.err.println("Could not create database on server: " + ex.getMessage());
                    }
                    // Connect to newly created database
                    connection = DriverManager.getConnection(URL_WITH_DB, USERNAME, PASSWORD);
                    System.out.println("MySQL Connection Established Successfully to: " + DATABASE_NAME);
                }
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database Connection Error: " + e.getMessage());
        }
        return connection;
    }

    /**
     * Executes a retrieval query (SELECT) given as a String parameter.
     * 
     * @param query SQL SELECT query string
     * @return ResultSet containing the query results, or null on error
     */
    public static ResultSet executeQuery(String query) {
        try {
            Connection conn = getConnection();
            if (conn != null) {
                Statement stmt = conn.createStatement();
                return stmt.executeQuery(query);
            }
        } catch (SQLException e) {
            System.err.println("Error executing retrieval query: " + e.getMessage());
        }
        return null;
    }

    /**
     * Executes a non-retrieval query (INSERT, UPDATE, DELETE, CREATE, etc.) given as a String parameter.
     * 
     * @param query SQL non-retrieval query string
     * @return number of affected rows, or -1 on error
     */
    public static int executeUpdate(String query) {
        try {
            Connection conn = getConnection();
            if (conn != null) {
                try (Statement stmt = conn.createStatement()) {
                    return stmt.executeUpdate(query);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error executing non-retrieval query: " + e.getMessage());
        }
        return -1;
    }
}
