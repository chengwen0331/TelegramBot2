package org.example;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.List;

public class Main {

    static int numOfProcess;
    static int quantumNum;
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
    static List<String> processIDList = new ArrayList<>();
    static List<Integer> burstTimeList = new ArrayList<>();
    static List<Integer> arrivalTimeList = new ArrayList<>();
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
    }

    public static void processInput(int numOfProcess, int quantumNum) {

        String sql = "INSERT INTO processnum_data(numOfProcess, quantumNum) VALUES(?,?)";
        try{
            Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, numOfProcess);
            pstmt.setInt(2, quantumNum);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void handleInput(int numOfProcess, String processID, int burstTime, int arrivalTime) {
        int data = selectData(numOfProcess);
        String sql = "INSERT INTO process_data(operationID, processID, burstTime, arrivalTime) VALUES(?,?,?,?)";
        try{
            Connection conn = connect();
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
        String sql = "SELECT operationID FROM processnum_data WHERE numOfProcess = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, number);  // Set the value for the placeholder
            ResultSet rs = pstmt.executeQuery();

            // Check if there is a result before trying to retrieve data
            if (rs.next()) {
                data = rs.getInt("operationID");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return data;
    }

    public static void selectAll(int num){
        String sql = "SELECT * FROM process_data INNER JOIN processnum_data ON process_data.operationID = processnum_data.operationID " +
                "WHERE processnum_data.numOfProcess = ?";
        processID= new String[num];
        burstTime= new int[num];
        arrivalTime= new int[num];

            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, num);  // Set the value for the placeholder
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    operationID = rs.getInt("process_data.operationID");
                    numOfProcess = rs.getInt("numOfProcess");
                    quantumNum = rs.getInt("quantumNum");
                    String currentProcessID = rs.getString("processID");
                    processIDList.add(currentProcessID);
                    int currentBurstTime = rs.getInt("burstTime");
                    burstTimeList.add(currentBurstTime);
                    int currentArrivalTime = rs.getInt("arrivalTime");
                    arrivalTimeList.add(currentArrivalTime);
                //scheduler.setNumOfProcess(rs.getInt("numOfProcess"));
                //scheduler.setQuantumNum(rs.getInt("quantumNum"));
                //scheduler.setBurstTime(rs.getInt("process_data.burstTime"));
                }
                processID = processIDList.toArray(new String[0]);
                burstTime = burstTimeList.stream().mapToInt(Integer::intValue).toArray();
                arrivalTime = arrivalTimeList.stream().mapToInt(Integer::intValue).toArray();
                processRetrievedData(operationID, numOfProcess, quantumNum, processID, burstTime, arrivalTime);
            } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void processRetrievedData(int operationID, int numOfProcess, int quantumNum, String [] processID, int [] burstTime, int [] arrivalTime) {
        // Process or store the retrieved data, e.g., in another class (Scheduler)
        Scheduler scheduler = new Scheduler(numOfProcess, quantumNum, processID, burstTime, arrivalTime);
        scheduler.runScheduler();
        Result resultCalculator = new Result(numOfProcess, burstTime, arrivalTime, scheduler.getTurnaroundTime(), scheduler.getStartTime());
        resultCalculator.calculateResults();
        resultCalculator.displayResults();
        averageWait = resultCalculator.getAvgWaitTime();
        averageTurn = resultCalculator.getAvgTurnTime();
        averageResponse = resultCalculator.getAvgResTime();
        waitTime = resultCalculator.getWaitTime();
        turnTime = resultCalculator.getTurnTime();
        responseTime = resultCalculator.getResponseTime();
        handleResult(operationID, averageWait, averageTurn, averageResponse);
        for(int i = 0; i < numOfProcess; i++){
            int response = responseTime[i];
            int wait = waitTime[i];
            int turn = turnTime[i];
            updateResult(operationID, response, wait, turn);
        }
    }

    public static void handleResult(int operationID, float averageWait, float averageTurn, float averageResponse) {
        String sql = "INSERT INTO process_result(operationID, avgResponse, avgWaiting, avgTurnaround) VALUES(?,?,?,?)";
        try{
            Connection conn = connect();
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

    public static void updateResult(int operationID, int responseTime, int waitTime, int turnTime) {
        String sql = "INSERT INTO process_data(responseTime, waitingTime, turnaroundTime) VALUES(?,?,?)" +
                "WHERE process_data.operationID = ?";
        try{
            Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, responseTime);
            pstmt.setInt(2, waitTime);
            pstmt.setInt(3, turnTime);
            pstmt.setInt(4, operationID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static Connection connect() {
        Connection conn = null;
        try {
            // db parameters
            String url = "jdbc:sqlite:C:/sqlite/realTime.db";
            // create a connection to the database
            conn = DriverManager.getConnection(url);

            System.out.println("Connection to SQLite has been established.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } /*finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }*/
        return conn;
    }


}