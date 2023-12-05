package org.example;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;

public class Main {
    public static void main(String[] args) throws TelegramApiException {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new simpleBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        connect();
    }

    public static void processInput(int numOfProcess, String[] processID) {
        // Your logic to process the input goes here
        System.out.println("Processing input...");
        System.out.println("Number of processes: " + numOfProcess);
        System.out.println("Process IDs:");
        for (String id : processID) {
            System.out.println(id);
        }

        // You can perform calculations or any other processing using the input
    }

    public static void connect() {
        Connection conn = null;
        try {
            // db parameters
            String url = "jdbc:sqlite:C:/sqlite/realTime.db";
            // create a connection to the database
            conn = DriverManager.getConnection(url);

            System.out.println("Connection to SQLite has been established.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

}