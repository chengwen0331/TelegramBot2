package org.example;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Class responsible for creating a new SQLite database.
 */
public class Create {
    /**
     * This method is to create a new SQLite database with the specified file name.
     *
     * @param fileName The name of the SQLite database file to be created.
     */
    public static void createNewDatabase(String fileName) {

        String url = "jdbc:sqlite:C:/sqlite/" + fileName;

        try {
            Connection conn = DriverManager.getConnection(url);
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * The main method to demonstrate creating a new SQLite database.
     *
     * @param args The command line arguments (not used in this example).
     */
    public static void main(String[] args) {
        createNewDatabase("telegrambot5.db");
    }
}
