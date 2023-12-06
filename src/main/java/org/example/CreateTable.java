package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
public class CreateTable {

    public void createNewTable() {
        // SQLite connection
        String url = "jdbc:sqlite:C:/sqlite/realTime.db";

        // SQL statement for creating a new table
        String sql1 = "CREATE TABLE IF NOT EXISTS processnum_data (\n"
                + " operationID INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + " numOfProcess INTEGER NOT NULL, \n"
                + " quantumNum INTEGER NOT NULL\n"
                + ");";

        String sql2 = "CREATE TABLE IF NOT EXISTS process_data (\n"
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + " operationID INTEGER,\n"
                + " processID TEXT, \n"
                + " burstTime INTEGER, \n"
                + " arrivalTime INTEGER, \n"
                + " responseTime INTEGER, \n"
                + " waitingTime INTEGER, \n"
                + " turnaroundTime INTEGER \n"
                + ");";

        String sql3 = "CREATE TABLE IF NOT EXISTS process_result (\n"
                + " id integer PRIMARY KEY AUTOINCREMENT,\n"
                + " operationID INTEGER,\n"
                + " avgResponse REAL, \n"
                + " avgWaiting REAL, \n"
                + " avgTurnaround REAL \n"
                + ");";

        try{
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            stmt.execute(sql1);
            stmt.execute(sql2);
            stmt.execute(sql3);
            System.out.println("Success");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /*public static void main(String[] args) {

        createNewTable();
    }*/
}

