package org.example;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class Main {

    static int operationID;
    static String [] processID;
    static int [] burstTime;
    static int [] arrivalTime;
    static int [] waitTime;
    static int [] turnTime;
    static int [] responseTime;
    static float averageWait = 0;
    static float averageResponse = 0;
    static float averageTurn = 0;
    static int data;
    static int id;
    static List<String> processIDList = new ArrayList<>();
    static List<Integer> burstTimeList = new ArrayList<>();
    static List<Integer> arrivalTimeList = new ArrayList<>();
    static List<Integer> responseTimeList = new ArrayList<>();
    static Connection conn = connect();
    public static void main(String[] args) throws TelegramApiException {

        connect();
        CreateTable generate = new CreateTable();
        generate.createNewTable();

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new simpleBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        // Add a shutdown hook to close the database connection
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                    System.out.println("Connection to SQLite has been closed.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }));
    }

    public static void processInput(int numOfProcess, int quantumNum) {

        String sql = "INSERT INTO processnum_data(numOfProcess, quantumNum) VALUES(?,?)";
        try{
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, numOfProcess);
            pstmt.setInt(2, quantumNum);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void handleInput(int numOfProcess, String processID, int burstTime, int arrivalTime) {
        data = selectData(numOfProcess);
        String sql = "INSERT INTO process_data(operationID, processID, burstTime, arrivalTime) VALUES(?,?,?,?)";
        try{
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, data);
            pstmt.setString(2, processID);
            pstmt.setInt(3, burstTime);
            pstmt.setInt(4, arrivalTime);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static int selectData(int number) {
        int data = 0;
        String sql = "SELECT operationID FROM processnum_data WHERE numOfProcess = ? ORDER BY operationID DESC LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, number);
            ResultSet rs = pstmt.executeQuery();

            // Check if there is a result before trying to retrieve data
            if (rs.next()) {
                data = rs.getInt("operationID");
                System.out.println("Operation ID is" + data);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return data;
    }

    public static void selectAll(int num, int quantum){
        String sql = "SELECT * FROM process_data WHERE operationID = ?";
        processID= new String[num];
        burstTime= new int[num];
        arrivalTime= new int[num];
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, data);
                ResultSet rs = pstmt.executeQuery();
                processIDList.clear();
                burstTimeList.clear();
                arrivalTimeList.clear();

                while (rs.next()) {
                    operationID = rs.getInt("operationID");
                    String currentProcessID = rs.getString("processID");
                    processIDList.add(currentProcessID);
                    int currentBurstTime = rs.getInt("burstTime");
                    burstTimeList.add(currentBurstTime);
                    int currentArrivalTime = rs.getInt("arrivalTime");
                    arrivalTimeList.add(currentArrivalTime);
                }

                processID = processIDList.toArray(new String[0]);
                burstTime = burstTimeList.stream().mapToInt(Integer::intValue).toArray();
                arrivalTime = arrivalTimeList.stream().mapToInt(Integer::intValue).toArray();
                processRetrievedData(data, num, quantum, processID, burstTime, arrivalTime);
            } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void processRetrievedData(int operationID, int numOfProcess, int quantumNum, String [] processID, int [] burstTime, int [] arrivalTime) {
        // Process or store the retrieved data, e.g., in another class (Scheduler)
        int[] tem_burstTime = new int[numOfProcess];
        for(int i = 0; i < numOfProcess; i++){
            tem_burstTime[i] = burstTime[i];
        }
        Scheduler scheduler = new Scheduler(numOfProcess, quantumNum, processID, burstTime, arrivalTime);
        scheduler.runScheduler();
        Result resultCalculator = new Result(numOfProcess, tem_burstTime, arrivalTime, scheduler.getTurnaroundTime(), scheduler.getStartTime());
        resultCalculator.calculateResults();
        resultCalculator.displayResults();
        averageWait = resultCalculator.getAvgWaitTime();
        averageTurn = resultCalculator.getAvgTurnTime();
        averageResponse = resultCalculator.getAvgResTime();
        waitTime = resultCalculator.getWaitTime();
        turnTime = resultCalculator.getTurnTime();
        responseTime = resultCalculator.getResponseTime();
        handleResult(operationID, averageWait, averageTurn, averageResponse);
        id = retrieveID(operationID);
        for(int i = 0; i < numOfProcess; i++){
            int response = responseTime[i];
            int wait = waitTime[i];
            int turn = turnTime[i];
            updateResult(id, operationID, response, wait, turn);
            id++;
        }
    }

    public static void handleResult(int operationID, float averageWait, float averageTurn, float averageResponse) {
        String sql = "INSERT INTO process_result(operationID, avgResponse, avgWaiting, avgTurnaround) VALUES(?,?,?,?)";
        try{
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, operationID);
            pstmt.setFloat(2, averageResponse);
            pstmt.setFloat(3, averageWait);
            pstmt.setFloat(4, averageTurn);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void updateResult(int id, int operationID, int responseTime, int waitTime, int turnTime) {
        System.out.println("checking" + responseTime);

        String sql = "UPDATE process_data SET responseTime = ?, waitingTime = ?, turnaroundTime = ? WHERE operationID = ? AND id = ?";
        try{
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, responseTime);
            pstmt.setInt(2, waitTime);
            pstmt.setInt(3, turnTime);
            pstmt.setInt(4, operationID);
            pstmt.setInt(5, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static int retrieveID(int operationID) {
        int value = 0;
        String sql = "SELECT id FROM process_data WHERE operationID = ? ORDER BY operationID ASC LIMIT 1";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, operationID);
            ResultSet rs = pstmt.executeQuery();

            // Check if there is a result before trying to retrieve data
            if (rs.next()) {
                value  = rs.getInt("id");
                System.out.println("ID is" + value );
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return value;
    }

    public static int [] getResTime(){
        String sql = "SELECT responseTime FROM process_data WHERE operationID = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, data);
            ResultSet rs = pstmt.executeQuery();
            responseTimeList.clear();

            while (rs.next()) {
                int resTime = rs.getInt("responseTime");
                responseTimeList.add(resTime);
                System.out.println("Retrieved responseTime: " + resTime);
            }

            responseTime = responseTimeList.stream().mapToInt(Integer::intValue).toArray();

            return responseTime;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return new int[0];
    }

    public static String processResTime(int numOfProcess){
        int processResponse [] = new int [numOfProcess];
        processResponse = getResTime();
        for(int j = 0; j < numOfProcess; j++){
            System.out.println("ProcessResponse" + processResponse[j]);
        }
        String response = "";
        for (int i = 0; i < numOfProcess; i++) {
            response += "Response Time for process " + (i + 1) + ": " + processResponse[i] + "\n";
        }
        return response;
    }

    public static Connection connect() {
        Connection conn = null;
        try {
            // db parameters
            String url = "jdbc:sqlite:C:/sqlite/telegrambot5.db";
            // create a connection to the database
            conn = DriverManager.getConnection(url);

            System.out.println("Connection to SQLite has been established.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

}