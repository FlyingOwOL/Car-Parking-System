package DAO;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnectionUtil {
    private static final String DB_URL_PROPERTY = "db.url";
    private static final String DB_USER_PROPERTY = "db.user";
    private static final String DB_PASSWORD_PROPERTY = "db.password";
    private static final String PROPERTIES_FILE = "db.properties";

    private static Properties properties = new Properties();

    static {
        try (InputStream input = DBConnectionUtil.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                System.err.println("Cannot find properties file: " + PROPERTIES_FILE);
            } else {
                properties.load(input);
            }
        } catch (IOException ex) {
            System.err.println("Error reading configuration file: " + ex.getMessage());
        }
    }

    //TEMP! Make sure this works on your end!
    public static void main(String[] args) {
        Connection testConnection = null;
        try {
            System.out.println("Attempting to load configuration...");

            // Note: The static block runs automatically, loading the properties.

            System.out.println("Connecting to URL: " + properties.getProperty(DB_URL_PROPERTY));

            testConnection = getConnection();

            if (testConnection != null) {
                System.out.println("\nSUCCESS: Connection established successfully!");
                System.out.println("   Database Product Name: " + testConnection.getMetaData().getDatabaseProductName());
                System.out.println("   Database Product Version: " + testConnection.getMetaData().getDatabaseProductVersion());
            }

        } catch (SQLException e) {
            System.err.println("\nFAILURE: Could not connect to the database.");
            System.err.println("   SQL State: " + e.getSQLState());
            System.err.println("   Reason: " + e.getMessage());
        } finally {
            closeConnection(testConnection);
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = properties.getProperty(DB_URL_PROPERTY);
        String user = properties.getProperty(DB_USER_PROPERTY);
        String password = properties.getProperty(DB_PASSWORD_PROPERTY);

        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw e;
        }
    }

    public static void closeConnection(Connection connection, java.sql.PreparedStatement ps, java.sql.ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (connection != null) connection.close();
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    public static void closeConnection(Connection connection, java.sql.PreparedStatement ps) {
        closeConnection(connection, ps, null);
    }

    public static void closeConnection(Connection connection) {
        closeConnection(connection, null, null);
    }
}
