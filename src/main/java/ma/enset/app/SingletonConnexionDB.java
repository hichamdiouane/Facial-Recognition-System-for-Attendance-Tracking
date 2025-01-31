package ma.enset.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SingletonConnexionDB {
    private static Connection con;
    private static final String DB_URL = "jdbc:sqlite:D:/ENSET/S3/Java/Project/back-end/attendance_system.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection(DB_URL);
            if (con != null) {
                System.out.println("Connected to SQLite database!");
            } else {
                System.err.println("Failed to connect to SQLite database.");
                throw new SQLException("Failed to connect to SQLite database.");
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error connecting to SQLite database:");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        return con;
    }

    public static void closeConnection() {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
                System.out.println("SQLite connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing SQLite connection:");
            e.printStackTrace();
        }
    }
}